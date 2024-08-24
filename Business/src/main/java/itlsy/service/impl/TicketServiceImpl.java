package itlsy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;


import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import io.seata.core.context.RootContext;
import itlsy.DistributedCache;
import itlsy.chain.AbstractChainContext;
import itlsy.constant.Index12306Constant;
import itlsy.constant.RedisKeyConstant;
import itlsy.context.LoginMemberContext;
import itlsy.dto.*;
import itlsy.entry.*;
import itlsy.enums.*;
import itlsy.exception.ServiceException;
import itlsy.feign.PayRemoteService;
import itlsy.feign.TicketOrderRemoteService;
import itlsy.feign.dto.*;
import itlsy.mapper.*;
import itlsy.req.*;
import itlsy.req.TicketPageQueryReqDTO;
import itlsy.resp.RefundTicketRespDTO;
import itlsy.resp.TicketOrderDetailRespDTO;
import itlsy.resp.TicketPurchaseRespDTO;
import itlsy.resp.TicketQueryRespDTO;
import itlsy.result.Result;
import itlsy.handler.select.TrainSeatTypeSelector;
import itlsy.service.SeatService;
import itlsy.service.TicketService;
import itlsy.handler.tokenbucket.TicketAvailabilityTokenBucket;
import itlsy.util.DateUtil;
import itlsy.util.SnowUtil;
import itlsy.util.TimeStringComparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static itlsy.constant.RedisKeyConstant.TRAIN_STATION_REMAINING_TICKET;

@Service
@Slf4j
@RequiredArgsConstructor
//TODO 修改
public class TicketServiceImpl implements TicketService {

    private final TicketMapper02 ticketMapper02;
    private final TicketMapper ticketMapper;
    private final TrainMapper trainMapper;
    private final TrainStationRelationMapper trainStationRelationMapper;
    private final TrainStationPriceMapper trainStationPriceMapper;
    private final StationMapper stationMapper;
    private final DistributedCache distributedCache;
    private final AbstractChainContext<PurchaseTicketReqDTO> purchaseTicketAbstractChainContext;
    private final AbstractChainContext<TicketPageQueryReqDTO> ticketPageQueryAbstractChainContext;
    private final TrainSeatTypeSelector trainSeatTypeSelector;
    private final TicketOrderRemoteService ticketOrderRemoteService;
    private final PayRemoteService payRemoteService;
    private final ConfigurableEnvironment environment;
    private final SeatService seatService;
    private final TicketAvailabilityTokenBucket ticketAvailabilityTokenBucket;
    private final RedissonClient redissonClient;

    @Value("${ticket.availability.cache-update.type:}")
    private String ticketAvailabilityCacheUpdateType;

    @Value("${framework.cache.redis.prefix:}")
    private String cacheRedisPrefix;


    /**
     * 会员购买车票后新增保存
     *
     * @param req
     */
    public void save(NumberTicketReq req) {
        log.info("seata的全局事务id:{}", RootContext.getXID());//与远程调用的id一致，表示在一个事务中
        DateTime now = DateTime.now();
        Ticket02 ticket = BeanUtil.copyProperties(req, Ticket02.class);
        ticket.setId(SnowUtil.getSnowflakeNextId());
        ticket.setCreateTime(now);
        ticket.setUpdateTime(now);
        ticketMapper02.insert(ticket);
    }

    /**
     * 根据条件分页查询车票V2高性能版本
     *
     * @param requestParam 分页查询车票请求参数
     * @return 查询车票返回结果
     * TODO quantity需要修改
     */
    public TicketQueryRespDTO queryList(TicketPageQueryReqDTO requestParam) {
        // 责任链模式 验证城市名称是否存在、不存在加载缓存以及出发日期不能小于当前日期等等
        ticketPageQueryAbstractChainContext.handler(TicketChainMarkEnum.TRAIN_QUERY_FILTER.name(), requestParam);
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        // 假设映射关系存储在Map<String, String>中，key为车站代码，value为车站名称
        Map<String, String> stationCodeToNameMap = getStationCodeToNameMap();
        ArrayList<String> stationDetails = new ArrayList<>();
        stationDetails.add(stationCodeToNameMap.get(requestParam.getFromStation()));
        stationDetails.add(stationCodeToNameMap.get(requestParam.getToStation()));
        ArrayList<TicketListDTO> seatResults = new ArrayList<>();
        TrainStationRelationExample relationExample = new TrainStationRelationExample();
        relationExample.createCriteria()
                .andStartRegionEqualTo(stationDetails.get(0))
                .andEndRegionEqualTo(stationDetails.get(1));
        //按照城市查询
        List<TrainStationRelation> trainStationRelations = trainStationRelationMapper.selectByExample(relationExample);
        //如果按城市无法找到，那就按照车站查询
        if (CollUtil.isEmpty(trainStationRelations)) {
            TrainStationRelationExample relationExample1 = new TrainStationRelationExample();
            relationExample1.createCriteria()
                    .andDepartureEqualTo(stationDetails.get(0))
                    .andArrivalEqualTo(stationDetails.get(1));
            trainStationRelations = trainStationRelationMapper.selectByExample(relationExample1);
        }

        for (TrainStationRelation each : trainStationRelations) {
            Train train = trainMapper.selectByPrimaryKey(each.getTrainId());
            TicketListDTO result = new TicketListDTO();
            result.setTrainId(String.valueOf(train.getId()));
            result.setTrainNumber(train.getTrainNumber());
            result.setDepartureTime(DateUtil.convertDateToLocalTime(each.getDepartureTime(), "HH:mm"));
            result.setArrivalTime(DateUtil.convertDateToLocalTime(each.getArrivalTime(), "HH:mm"));
            result.setDuration(DateUtil.calculateHourDifference(each.getDepartureTime(), each.getArrivalTime()));
            result.setDeparture(each.getDeparture());
            result.setArrival(each.getArrival());
            result.setDepartureFlag(each.getDepartureFlag());
            result.setArrivalFlag(each.getArrivalFlag());
            result.setTrainType(train.getTrainType());
            result.setTrainBrand(train.getTrainBrand());
            if (StrUtil.isNotBlank(train.getTrainTag())) {
                result.setTrainTags(StrUtil.split(train.getTrainTag(), ","));
            }
            long betweenDay = cn.hutool.core.date.DateUtil.betweenDay(each.getDepartureTime(), each.getArrivalTime(), false);
            result.setDaysArrived((int) betweenDay);
            result.setSaleStatus(new Date().after(train.getSaleTime()) ? 0 : 1);
            LocalDate localDateSaleTime = train.getSaleTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            result.setSaleTime(LocalDateTimeUtil.format(localDateSaleTime, "HH:mm"));
            seatResults.add(result);
        }
        for (TicketListDTO each : seatResults) {
            TrainStationPriceExample priceExample = new TrainStationPriceExample();
            priceExample.createCriteria()
                    .andTrainIdEqualTo(Long.valueOf(each.getTrainId()))
                    .andDepartureEqualTo(each.getDeparture())
                    .andArrivalEqualTo(each.getArrival());
            List<TrainStationPrice> trainStationPrices = trainStationPriceMapper.selectByExample(priceExample);
            ArrayList<SeatClassDTO> seatClassList = new ArrayList<>();
            trainStationPrices.forEach(item -> {
                SeatClassDTO build = SeatClassDTO.builder()
                        .type(item.getSeatType())
                        .price(new BigDecimal(item.getPrice()).divide(new BigDecimal("100"), 1, RoundingMode.HALF_UP))
                        .candidate(false)
                        .quantity(1)
                        .build();
                seatClassList.add(build);

            });
            each.setSeatClassList(seatClassList);
        }
        return TicketQueryRespDTO.builder()
                .trainList(seatResults)
                .departureStationList(buildDepartureStationList(seatResults))
                .arrivalStationList(buildArrivalStationList(seatResults))
                .trainBrandList(buildTrainBrandList(seatResults))
                .seatClassTypeList(buildSeatClassList(seatResults))
                .build();

    }

    @Override
    public void cancelTicketOrder(CancelTicketOrderReqDTO requestParam) {
        Result<Void> cancelTicketOrder = ticketOrderRemoteService.cancelTicketOrder(requestParam);
         if (cancelTicketOrder.isSuccess()&&!StrUtil.equals(ticketAvailabilityCacheUpdateType,"binlog")){
            Result<itlsy.feign.dto.TicketOrderDetailRespDTO> ticketOrderDetailResult = ticketOrderRemoteService.queryTicketOrderByOrderSn(requestParam.getOrderSn());
            itlsy.feign.dto.TicketOrderDetailRespDTO ticketOrderDetail = ticketOrderDetailResult.getData();
            String trainId = String.valueOf(ticketOrderDetail.getTrainId());
            String departure = ticketOrderDetail.getDeparture();
            String arrival = ticketOrderDetail.getArrival();
            List<TicketOrderPassengerDetailRespDTO> trainPurchaseTicketResults = ticketOrderDetail.getPassengerDetails();
            try {
                seatService.unlock(trainId,departure,arrival,BeanUtil.copyToList(trainPurchaseTicketResults,TrainPurchaseTicketRespDTO.class));
            } catch (Exception e) {
                log.error("[取消订单] 订单号：{} 回滚列车DB座位状态失败", requestParam.getOrderSn(), e);
                throw e;
            }
            ticketAvailabilityTokenBucket.rollbackInBucket(ticketOrderDetail);
             try {
                 StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
                 Map<Integer, List<TicketOrderPassengerDetailRespDTO>> seatTypeMap = trainPurchaseTicketResults.stream()
                         .collect(Collectors.groupingBy(TicketOrderPassengerDetailRespDTO::getSeatType));
                 List<RouteDTO> routeDTOList = seatService.listTakeoutTrainStationRoute(trainId, departure, arrival);
                 routeDTOList.forEach(each->{
                     String keySuffix = StrUtil.join("_", trainId, each.getStartStation(), each.getEndStation());
                     seatTypeMap.forEach((seatType,ticketOrderPassengerDetailRespDTOList)->{
                         stringRedisTemplate.opsForHash()
                                 .increment(TRAIN_STATION_REMAINING_TICKET+keySuffix,String.valueOf(seatType),ticketOrderPassengerDetailRespDTOList.size());
                     });
                 });
             } catch (Exception e) {
                 log.error("[取消关闭订单] 订单号：{} 回滚列车Cache余票失败", requestParam.getOrderSn(), e);
                 throw e;
             }
         }
    }

    @Override
    public RefundTicketRespDTO commonTicketRefund(RefundTicketReqDTO requestParam) {
        // 责任链模式，验证 1：参数必填
        //refundReqDTOAbstractChainContext.handler(TicketChainMarkEnum.TRAIN_REFUND_TICKET_FILTER.name(),requestParam);
        Result<itlsy.feign.dto.TicketOrderDetailRespDTO> orderDetailRespDTOResult = ticketOrderRemoteService.queryTicketOrderByOrderSn(requestParam.getOrderSn());
        if (!orderDetailRespDTOResult.isSuccess() && ObjectUtil.isNull(orderDetailRespDTOResult.getData())) {
            throw new ServiceException("车票订单不存在");
        }
        itlsy.feign.dto.TicketOrderDetailRespDTO ticketOrderDetailRespDTO = orderDetailRespDTOResult.getData();
        List<TicketOrderPassengerDetailRespDTO> passengerDetails = ticketOrderDetailRespDTO.getPassengerDetails();
        if (CollUtil.isEmpty(passengerDetails)) {
            throw new ServiceException("车票订单乘客信息不存在");
        }
        RefundReqDTO refundReqDTO = new RefundReqDTO();
        if (RefundTypeEnum.PARTIAL_REFUND.getType().equals(requestParam.getType())) {
            TicketOrderItemQueryReqDTO ticketOrderItemQueryReqDTO = new TicketOrderItemQueryReqDTO();
            ticketOrderItemQueryReqDTO.setOrderSn(requestParam.getOrderSn());
            ticketOrderItemQueryReqDTO.setOrderItemRecordIds(requestParam.getSubOrderRecordIdReqList());
            Result<List<TicketOrderPassengerDetailRespDTO>> queryTicketItemOrderById = ticketOrderRemoteService.queryTicketItemOrderById(ticketOrderItemQueryReqDTO);
            List<TicketOrderPassengerDetailRespDTO> partialRefundPassengerDetails = passengerDetails.stream()
                    .filter(item -> queryTicketItemOrderById.getData().contains(item))
                    .toList();
            refundReqDTO.setRefundTypeEnum(RefundTypeEnum.PARTIAL_REFUND);
            refundReqDTO.setRefundDetailReqDTOList(partialRefundPassengerDetails);
        } else if (RefundTypeEnum.FULL_REFUND.getType().equals(requestParam.getType())) {
            refundReqDTO.setRefundTypeEnum(RefundTypeEnum.FULL_REFUND);
            refundReqDTO.setRefundDetailReqDTOList(passengerDetails);
        }
        if (CollUtil.isNotEmpty(passengerDetails)) {
            int partialRefundAmount = passengerDetails.stream()
                    .mapToInt(TicketOrderPassengerDetailRespDTO::getAmount)
                    .sum();
            refundReqDTO.setRefundAmount(partialRefundAmount);
        }
        refundReqDTO.setOrderSn(requestParam.getOrderSn());
        Result<RefundRespDTO> refundRespDTOResult = payRemoteService.commonRefund(refundReqDTO);
        if (!refundRespDTOResult.isSuccess() && Objects.isNull(refundRespDTOResult.getData())) {
            throw new ServiceException("车票订单退款失败");
        }
        return null; // 暂时返回空实体
    }

    private final Cache<String, Object> tokenTicketsRefreshMap = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();
    private final Cache<String,ReentrantLock> localLockMap=Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    @Override
    public TicketPurchaseRespDTO purchaseTickets(PurchaseTicketReqDTO requestParam) {
        //责任链模式，验证 1：参数必填 2：参数正确性 3：乘客是否已买当前车次等...
        purchaseTicketAbstractChainContext.handler(TicketChainMarkEnum.TRAIN_PURCHASE_TICKET_FILTER.name(), requestParam);
        //令牌限流
        TokenResultDTO tokenResult = ticketAvailabilityTokenBucket.takeTokenFromBucket(requestParam);
        if (tokenResult.getTokenIsNull()) {
            Object ifPresentObj = tokenTicketsRefreshMap.getIfPresent(requestParam.getTrainId());
            if (ifPresentObj == null) {
                synchronized (TicketService.class) {
                    if (tokenTicketsRefreshMap.getIfPresent(requestParam.getTrainId()) == null) {
                        ifPresentObj = new Object();
                        tokenTicketsRefreshMap.put(requestParam.getTrainId(), ifPresentObj);
                        tokenIsNullRefreshToken(requestParam, tokenResult);
                    }
                }
            }
            throw new ServiceException("列车站点已无余票");
        }
        ArrayList<ReentrantLock> localLockList = new ArrayList<>();
        ArrayList<RLock> distributedLockList = new ArrayList<>();
        Map<Integer, List<PurchaseTicketPassengerDetailDTO>> seatTypeMap = requestParam.getPassengers().stream()
                .collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType));
        seatTypeMap.forEach((seatType,count)->{
            String lockKey = environment.resolvePlaceholders(String.format(RedisKeyConstant.LOCK_PURCHASE_TICKETS_V2, requestParam.getTrainId(), seatType));
            ReentrantLock localLock = localLockMap.getIfPresent(lockKey);
            if (localLock==null){
                synchronized (TicketService.class){
                    if ((localLock=localLockMap.getIfPresent(lockKey))==null){
                        localLock = new ReentrantLock(true);
                        localLockMap.put(lockKey, localLock);
                    }
                }
            }
            localLockList.add(localLock);
            RLock distributedLock = redissonClient.getFairLock(lockKey);
            distributedLockList.add(distributedLock);
        });
        try {
            localLockList.forEach(ReentrantLock::lock);
            distributedLockList.forEach(RLock::lock);
            return executePurchaseTickets(requestParam);
        } finally {
            localLockList.forEach(localLock -> {
                try {
                    localLock.unlock();
                } catch (Throwable ignored) {
                }
            });
            distributedLockList.forEach(distributedLock -> {
                try {
                    distributedLock.unlock();
                } catch (Throwable ignored) {
                }
            });
        }
    }

    private final ScheduledExecutorService tokenIsNullRefreshExecutor = Executors.newScheduledThreadPool(1);

    private void tokenIsNullRefreshToken(PurchaseTicketReqDTO requestParam, TokenResultDTO tokenResult) {
        RLock lock = redissonClient.getLock(String.format(RedisKeyConstant.LOCK_TOKEN_BUCKET_ISNULL, requestParam.getTrainId()));
        if (!lock.tryLock()) {
            return;
        }
        tokenIsNullRefreshExecutor.schedule(() -> {
            try {
                ArrayList<Integer> seatTypes = new ArrayList<>();
                HashMap<Integer, Integer> tokenCountMap = new HashMap<>();
                tokenResult.getTokenIsNullSeatTypeCounts().stream()
                        .map(item -> item.split("_"))
                        .forEach(each -> {
                            int seatType = Integer.parseInt(each[0]);
                            seatTypes.add(seatType);
                            tokenCountMap.put(seatType, Integer.parseInt(each[1]));
                        });
                List<SeatTypeCountDTO> seatTypeCountDTOList = seatService.listSeatTypeCount(Long.parseLong(requestParam.getTrainId()), requestParam.getDeparture(), requestParam.getArrival(), seatTypes);
                for (SeatTypeCountDTO each : seatTypeCountDTOList) {
                    Integer tokenCount = tokenCountMap.get(each.getSeatType());
                    if (tokenCount < each.getSeatCount()) {
                        ticketAvailabilityTokenBucket.delTokenInBucket(requestParam);
                        break;
                    }
                }
            } finally {
                lock.unlock();
            }

        }, 10, TimeUnit.SECONDS);
    }


    /**
     * 执行购买车票
     *
     * @param requestParam 车票购买请求参数
     * @return 订单号
     */
    @Transactional(rollbackFor = Throwable.class)
    public TicketPurchaseRespDTO executePurchaseTickets(PurchaseTicketReqDTO requestParam) {
        List<TicketOrderDetailRespDTO> ticketOrderDetailResults = new ArrayList<>();
        System.out.println(RedisKeyConstant.TRAIN_INFO + requestParam.getTrainId());
        Train train = distributedCache.safeGet(
                RedisKeyConstant.TRAIN_INFO + requestParam.getTrainId(),
                Train.class,
                () -> trainMapper.selectByPrimaryKey(Long.valueOf(requestParam.getTrainId())),
                Index12306Constant.ADVANCE_TICKET_DAY,
                TimeUnit.DAYS
        );
        List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults = trainSeatTypeSelector.select(train.getTrainType(), requestParam);
        List<Ticket> ticketList = trainPurchaseTicketResults.stream()
                .map(item -> Ticket.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .username(LoginMemberContext.getUserName())
                        .trainId(Long.valueOf(requestParam.getTrainId()))
                        .carriageNumber(item.getCarriageNumber())
                        .seatNumber(item.getSeatNumber())
                        .passengerId(item.getPassengerId())
                        .ticketStatus(TicketStatusEnum.UNPAID.getCode())
                        .delFlag(0)
                        //到后期采用Aop填充
                        .createTime(new Date())
                        .updateTime(new Date())
                        .build())
                .toList();
        ticketMapper.insertBatch(ticketList);
        Result<String> ticketOrderResult;
        try {
            List<TicketOrderItemCreateRemoteReqDTO> orderItemCreateRemoteReqDTOList = new ArrayList<>();
            trainPurchaseTicketResults.forEach(item -> {
                TicketOrderItemCreateRemoteReqDTO orderItemCreateRemoteReqDTO = TicketOrderItemCreateRemoteReqDTO.builder()
                        .amount(item.getAmount())
                        .carriageNumber(item.getCarriageNumber())
                        .seatNumber(item.getSeatNumber())
                        .idCard(item.getIdCard())
                        .idType(item.getIdType())
                        .phone(item.getPhone())
                        .seatType(item.getSeatType())
                        .ticketType(item.getUserType())
                        .realName(item.getRealName())
                        .build();
                TicketOrderDetailRespDTO ticketOrderDetailRespDTO = TicketOrderDetailRespDTO.builder()
                        .amount(item.getAmount())
                        .carriageNumber(item.getCarriageNumber())
                        .seatNumber(item.getSeatNumber())
                        .idCard(item.getIdCard())
                        .idType(item.getIdType())
                        .seatType(item.getSeatType())
                        .ticketType(item.getUserType())
                        .realName(item.getRealName())
                        .build();
                orderItemCreateRemoteReqDTOList.add(orderItemCreateRemoteReqDTO);
                ticketOrderDetailResults.add(ticketOrderDetailRespDTO);
            });
            TrainStationRelationExample relationExample = new TrainStationRelationExample();
            relationExample.createCriteria()
                    .andTrainIdEqualTo(Long.valueOf(requestParam.getTrainId()))
                    .andDepartureEqualTo(requestParam.getDeparture())
                    .andArrivalEqualTo(requestParam.getArrival());
            TrainStationRelation trainStationRelation = trainStationRelationMapper.selectByExample(relationExample).get(0);
            TicketOrderCreateRemoteReqDTO orderCreateRemoteReqDTO = TicketOrderCreateRemoteReqDTO.builder()
                    .departure(requestParam.getDeparture())
                    .arrival(requestParam.getArrival())
                    .orderTime(new Date())
                    .source(SourceEnum.INTERNET.getCode())
                    .trainNumber(train.getTrainNumber())
                    .departureTime(trainStationRelation.getDepartureTime())
                    .arrivalTime(trainStationRelation.getArrivalTime())
                    .ridingDate(trainStationRelation.getDepartureTime())
                    .userId(String.valueOf(LoginMemberContext.getId()))
                    .username(LoginMemberContext.getUserName())
                    .trainId(Long.parseLong(requestParam.getTrainId()))
                    .ticketOrderItems(orderItemCreateRemoteReqDTOList)
                    .build();
            ticketOrderResult = ticketOrderRemoteService.createTicketOrder(orderCreateRemoteReqDTO);

            if (!ticketOrderResult.isSuccess() || StrUtil.isBlank(ticketOrderResult.getData())) {
                log.error("订单服务调用失败，返回结果：{}", ticketOrderResult.getMessage());
                throw new ServiceException("订单服务调用失败");
            }
        } catch (Throwable ex) {
            log.error("远程调用订单服务创建错误，请求参数：{}", JSON.toJSONString(requestParam), ex);
            throw ex;
        }
        return new TicketPurchaseRespDTO(ticketOrderResult.getData(), ticketOrderDetailResults);
    }

    private Map<String, String> getStationCodeToNameMap() {
        Map<String, String> result = new HashMap<>();
        StationExample stationExample = new StationExample();
        stationExample.createCriteria();
        List<Station> stations = stationMapper.selectByExample(stationExample);
        stations.forEach(item -> result.put(item.getCode(), item.getName()));
        return result;
    }

    private List<Integer> buildSeatClassList(List<TicketListDTO> ticketListDTOS) {
        Set<Integer> resultSeatClassList = new HashSet<>();
        for (TicketListDTO each : ticketListDTOS) {
            for (SeatClassDTO item : each.getSeatClassList()) {
                resultSeatClassList.add(item.getType());
            }
        }
        return resultSeatClassList.stream().toList();
    }

    private List<Integer> buildTrainBrandList(List<TicketListDTO> ticketListDTOS) {
        HashSet<Integer> trainBrandSet = new HashSet<>();
        for (TicketListDTO ticketListDTO : ticketListDTOS) {
            if (StrUtil.isNotBlank(ticketListDTO.getTrainBrand())) {
                trainBrandSet.addAll(StrUtil.split(ticketListDTO.getTrainBrand(), ",").stream().map(Integer::parseInt).toList());
            }
        }
        return trainBrandSet.stream().toList();
    }

    private List<String> buildArrivalStationList(List<TicketListDTO> ticketListDTOS) {
        return ticketListDTOS.stream().map(TicketListDTO::getArrival).distinct().collect(Collectors.toList());
    }

    private List<String> buildDepartureStationList(List<TicketListDTO> ticketListDTOS) {
        return ticketListDTOS.stream().map(TicketListDTO::getDeparture).distinct().collect(Collectors.toList());
    }

    public void delete(Long id) {
        ticketMapper02.deleteByPrimaryKey(id);
    }
}