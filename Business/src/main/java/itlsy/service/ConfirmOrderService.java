package itlsy.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import itlsy.dto.ConfirmOrderMQDto;
import itlsy.entry.*;
import itlsy.enums.ConfirmOrderStatusEnum;
import itlsy.enums.SeatColEnum;
import itlsy.enums.SeatTypeEnum;
import itlsy.exception.BusinessException;
import itlsy.exception.BusinessExceptionEnum;
import itlsy.mapper.ConfirmOrderMapper;
import itlsy.req.ConfirmOrderDoReq;
import itlsy.req.ConfirmOrderQueryReq;
import itlsy.req.ConfirmOrderTicketReq;
import itlsy.resp.ConfirmOrderQueryResp;
import itlsy.resp.PageResp;
import itlsy.util.SnowUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Autowired
    private ConfirmOrderMapper confirmOrderMapper;

    @Autowired
    private DailyTrainTicketService dailyTrainTicketService;

    @Autowired
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Autowired
    private DailyTrainSeatService dailyTrainSeatService;

    @Autowired
    private AfterConfirmOrderService afterConfirmOrderService;

    @Autowired
    private RedissonClient redissonClient;//主要用于分布式锁

    @Autowired
    private SkTokenService skTokenService;

    public void save(ConfirmOrderDoReq req) {
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(confirmOrder.getId())) {
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        } else {
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }
    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req) {
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id desc");
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);

        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);

        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 查询当前id之前排队的人数
     * @param id
     * @return
     */

    public Integer queryLineCount(Long id) {
        ConfirmOrder confirmOrder = confirmOrderMapper.selectByPrimaryKey(id);
        ConfirmOrderStatusEnum statusEnum = EnumUtil.getBy(ConfirmOrderStatusEnum::getCode, confirmOrder.getStatus());
        int result=switch (statusEnum){
            case PENDING -> 0;
            case SUCCESS -> -1;
            case FAILURE -> -2;
            case EMPTY -> -3;
            case CANCEL -> -4;
            case INIT -> 999 ;
        };
        if (result == 999) {
            // 排在第几位，下面的写法：where a=1 and (b=1 or c=1) 等价于 where (a=1 and b=1) or (a=1 and c=1)
            ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
            confirmOrderExample.or().andDateEqualTo(confirmOrder.getDate())
                    .andTrainCodeEqualTo(confirmOrder.getTrainCode())
                    .andCreateTimeLessThan(confirmOrder.getCreateTime())
                    .andStatusEqualTo(ConfirmOrderStatusEnum.INIT.getCode());
            confirmOrderExample.or().andDateEqualTo(confirmOrder.getDate())
                    .andTrainCodeEqualTo(confirmOrder.getTrainCode())
                    .andCreateTimeLessThan(confirmOrder.getCreateTime())
                    .andStatusEqualTo(ConfirmOrderStatusEnum.PENDING.getCode());
            return Math.toIntExact(confirmOrderMapper.countByExample(confirmOrderExample));
        } else {
            return result;
        }
    }


    /**
     * 取消排队，只有I状态才能取消排队，所以按状态更新
     * @param id
     */
    public Integer cancel(Long id) {
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.createCriteria().andIdEqualTo(id).andStatusEqualTo(ConfirmOrderStatusEnum.INIT.getCode());
        ConfirmOrder confirmOrder = new ConfirmOrder();
        confirmOrder.setStatus(ConfirmOrderStatusEnum.CANCEL.getCode());
        return confirmOrderMapper.updateByExampleSelective(confirmOrder, confirmOrderExample);
    }


    //在下面的方法上加上synchronized只能解决单机锁的问题，因为其是本地锁只锁住当前对象，无法解决多个节点下超卖问题
    @Async //外部类调用此方法时会另开一个线程处理此方法(同类方法的调用会失效)
    @SentinelResource(value = "doConfirm", blockHandler = "doConfirmBlockHandler")//blockHandler表示被限流或系统保护拦截后的处理方式
    public void doConfirm(ConfirmOrderMQDto dto) {
        MDC.put("LOG_ID",dto.getLogId());//异步调用会丢失logId所有要重新加入
        LOG.info("异步出票开始：{}",dto);
        //设置分布式锁
        String lockKey = DateUtil.formatDate(dto.getDate()) + "-" + dto.getTrainCode();//类似于redis中的setnx加分布式锁

        //下面的代码会有超时后超卖问题
        /* Boolean redisLock =  redisLock = redisTemplate.opsForValue().setIfAbsent(key, key, 5, TimeUnit.SECONDS);//其中的value无所谓填什么值
        if (redisLock) {
            LOG.info("获取到分布式锁");
        } else
            LOG.info("获取分布式锁失败");
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_ERROR);
            }*/

        RLock lock = null;
        //redisson自带看门狗
        lock = redissonClient.getLock(lockKey);

        //强锁逻辑要写在try语句之前
        /**
         * l - 等待获得锁的时间(最大尝试获得锁时间)，超时返回false
         * l1 - 释放锁之前占用锁的时间
         * TimeUnit - 时间单位
         * boolean tryLock = lock.tryLock(0, 10, TimeUnit.SECONDS);//不带看门狗
         */
        boolean tryLock = false;//带看门狗机制,锁快过期时自动加时
        try {
            tryLock = lock.tryLock(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        try {
            if (tryLock) {
                LOG.info("获取到分布式锁");
            } else {
                LOG.info("没抢到锁，有其它消费线程正在出票，不做任何处理");
                //这里就可能出现没有拿到锁的线程将别人的锁释放掉所以要在finally语句块中加入lock.isHeldByCurrentThread()条件
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_ERROR);
            }
            while (true){
                ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
                confirmOrderExample.setOrderByClause("id asc");
                confirmOrderExample.createCriteria().andDateEqualTo(dto.getDate())
                        .andTrainCodeEqualTo(dto.getTrainCode())
                        .andStatusEqualTo(ConfirmOrderStatusEnum.INIT.getCode());
                PageHelper.startPage(1,5);
                List<ConfirmOrder> list = confirmOrderMapper.selectByExampleWithBLOBs(confirmOrderExample);

                if (CollUtil.isEmpty(list)) {
                    LOG.info("没有需要处理的订单，结束循环");
                    break;
                } else {
                    LOG.info("本次处理{}条订单", list.size());
                }
                list.forEach(confirmOrder ->{
                    try {
                        sell(confirmOrder);
                    } catch (BusinessException e) {
                        if (e.getE().equals(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR)) {
                            LOG.info("本订单余票不足，继续售卖下一个订单");
                            confirmOrder.setStatus(ConfirmOrderStatusEnum.EMPTY.getCode());
                            updateStatus(confirmOrder);
                        } else {
                            throw e;
                        }
                    }
                });
            }
        } catch (Exception e) {
            LOG.error("购票异常", e);
        } finally {
            LOG.info("购票成功，释放分布式锁");
            if (lock != null && lock.isHeldByCurrentThread()) {//*用于解决当票数不足时一个线程将另一个线程的锁释放*
                lock.unlock();
            }
        }
    }

    /**
     * 更新订单状态
     * @param confirmOrder
     */
    private void updateStatus(ConfirmOrder confirmOrder) {
        ConfirmOrder confirmOrder1 = new ConfirmOrder();
        confirmOrder1.setId(confirmOrder.getId());
        confirmOrder1.setStatus(confirmOrder.getStatus());
        confirmOrder1.setUpdateTime(new Date());
        confirmOrderMapper.updateByPrimaryKeySelective(confirmOrder1);
    }

    /**.
     * 售票
     * @param confirmOrder
     */
    private void sell(ConfirmOrder confirmOrder) {

        // 为了演示排队效果，每次出票增加200毫秒延时
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 构造ConfirmOrderDoReq
        ConfirmOrderDoReq req = new ConfirmOrderDoReq();
        req.setNumberId(confirmOrder.getNumberId());
        req.setDate(confirmOrder.getDate());
        req.setTrainCode(confirmOrder.getTrainCode());
        req.setStart(confirmOrder.getStart());
        req.setEnd(confirmOrder.getEnd());
        req.setDailyTrainTicketId(confirmOrder.getDailyTrainTicketId());
        req.setTickets(JSON.parseArray(confirmOrder.getTickets(), ConfirmOrderTicketReq.class));
        req.setLogId("");
        // 省略业务数据校验，如：车次是否存在，余票是否存在，车次是否在有效期内，tickets条数>0，同乘客同车次是否已买过

        LOG.info("将确认订单更新成处理中，避免重复处理，confirm_order.id: {}", confirmOrder.getId());
        confirmOrder.setStatus(ConfirmOrderStatusEnum.PENDING.getCode());
        updateStatus(confirmOrder);
        List<ConfirmOrderTicketReq> tickets = req.getTickets();
            /*// 保存确认订单表，状态初始
            Date date = new Date();
            ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class, "tickets");
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            //confirmOrder.setNumberId(LoginMemberContext.getId());
            confirmOrder.setNumberId(req.getNumberId());
            confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
            confirmOrder.setCreateTime(date);
            confirmOrder.setUpdateTime(date);
            confirmOrder.setTickets(JSON.toJSONString(tickets));
            confirmOrderMapper.insert(confirmOrder);*/

       /* //从数据库中查
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id asc");
        confirmOrderExample.createCriteria().andDateEqualTo(req.getDate())
                .andMemberIdEqualTo(req.getNumberId())
                .andStatusEqualTo(ConfirmOrderStatusEnum.INIT.getCode())
                .andTrainCodeEqualTo(req.getTrainCode());
        List<ConfirmOrder> list = confirmOrderMapper.selectByExampleWithBLOBs(confirmOrderExample);
        ConfirmOrder confirmOrder = null;
        if (CollUtil.isEmpty(list)){
            LOG.info("没有找到对应的订单信息");
        }else {
            LOG.info("本次处理:{}条确认订单", list.size());
            confirmOrder=list.get(0);
        }*/

        // 查出余票记录，需要得到真实的库存
        DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(req.getDate(), req.getTrainCode(), req.getStart(), req.getEnd());
        LOG.info("余票信息:{}", dailyTrainTicket.toString());

        // 扣减余票数量，并判断余票是否足够
        reduceTicket(req, dailyTrainTicket);

        //存储最终的购票结果
        ArrayList<DailyTrainSeat> finalSeatList = new ArrayList<>();

        /*1.计算相对第一个座位的偏移值
         * 2.比如选择的是C1，D2,则偏移量为[0.5]
         * 3.比如选择的是A1，B1，C1，则偏移量为[0,1,2]
         */
        ConfirmOrderTicketReq ticketReq0 = tickets.get(0);
        if (StrUtil.isNotBlank(ticketReq0.getSeat())) {
            LOG.info("本次购票有选座");

            //查出本次选座的座位类型都有哪些列，用于计算所选座位与第一个座位的偏移值
            List<SeatColEnum> colsByType = SeatColEnum.getColsByType(ticketReq0.getSeatTypeCode());
            LOG.info("本次选座座位类型包含的列有：{}", colsByType);

            //组成和前端两排选座一样的列表，用于做参照的作为列表，如:referSeatList={A1,C1,D1,F1,A2,C2,D2,F2}
            List<String> referSeatList = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                for (SeatColEnum seatColEnum : colsByType) {
                    referSeatList.add(seatColEnum.getCode() + (i + 1));
                }
            }
            LOG.info("用于作为参照的两排座位列表：{}", referSeatList);

            //先计算绝对偏移值，即参照作为在列表中的位置索引,如：绝对偏移[1,5]相对偏移为[1-1,5-1]=[0,4]
            ArrayList<Integer> absoluteOffSet = new ArrayList<>();
            for (ConfirmOrderTicketReq ticket : tickets) {
                String seatCode = ticket.getSeat();
                int index = referSeatList.indexOf(seatCode);
                absoluteOffSet.add(index);
            }
            LOG.info("计算得到所有的绝对偏移值为：{}", absoluteOffSet);

            //在计算相对偏移值，即第一个座位到所选座位的偏移值
            ArrayList<Integer> relativeOffSet = new ArrayList<>();
            for (Integer data : absoluteOffSet) {
                int index = data - absoluteOffSet.get(0);
                relativeOffSet.add(index);
            }
            LOG.info("计算得到所有的相对偏移值为：{}", relativeOffSet);
            //ticketReq0.getSeat().split("")[0]意思是将‘A1’变为【A,1】后取第一位
            getSeat(finalSeatList, req.getDate(), req.getTrainCode(), ticketReq0.getSeatTypeCode(), ticketReq0.getSeat().split("")[0], relativeOffSet, dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());

        } else {
            LOG.info("本次购票没有有选座");
            for (ConfirmOrderTicketReq ticket : tickets) {
                getSeat(finalSeatList, req.getDate(), req.getTrainCode(), ticket.getSeatTypeCode(), null, null, dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());
            }

        }
        LOG.info("最终的选座结果为：{}", finalSeatList);

        // 选中座位后事务处理：
        // 座位表修改售卖情况sell；
        // 余票详情表修改余票；
        // 为会员增加购票记录
        // 更新确认订单为成功
        try {
            afterConfirmOrderService.afterDoConfirm(dailyTrainTicket, finalSeatList, tickets, confirmOrder);
        } catch (Exception e) {
            LOG.error("购票失败事务回滚", e);
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_EXCEPTION);
        }
    }

    /**
     * 获取座位，如果有选座则一次性选完，如果无选座则一个一个挑
     *
     * @param date
     * @param trainCode
     * @param SeatType
     * @param column
     * @param offsetList
     */
    private void getSeat(ArrayList<DailyTrainSeat> finalSeatList, Date date, String trainCode, String SeatType, String column, List<Integer> offsetList, Integer startIndex, Integer endIndex) {
        List<DailyTrainCarriage> dailyTrainCarriagesList = dailyTrainCarriageService.selectBySeatType(date, trainCode, SeatType);
        LOG.info("共查出{}个符合条件的车厢", dailyTrainCarriagesList.size());
        //中间变量临时存储有效座位
        List<DailyTrainSeat> getSeatList = new ArrayList<>();
        // 一个车箱一个车箱的获取座位数据
        for (DailyTrainCarriage trainCarriage : dailyTrainCarriagesList) {
            LOG.info("开始从第{}车厢选座", trainCarriage.getIndex());
            CollUtil.clear(getSeatList);//换车箱时清空临时数据
            List<DailyTrainSeat> trainSeatList = dailyTrainSeatService.selectByCarriage(date, trainCode, trainCarriage.getIndex());
            LOG.info("{}号车厢的座位数为{}", trainCarriage.getIndex(), trainSeatList.size());
            for (DailyTrainSeat dailyTrainSeat : trainSeatList) {

                // 判断是否为有效座位,即不能被选中过
                boolean alreadySelected = false;
                for (DailyTrainSeat trainSeat : finalSeatList) {
                    if (trainSeat.getId().equals(dailyTrainSeat.getId())) {
                        alreadySelected = true;
                        break;
                    }
                }

                if (alreadySelected) {
                    LOG.info("座位{}已经被选过，跳过选下一个座位", dailyTrainSeat.getCarriageSeatIndex());
                    continue;
                }

                if (StrUtil.isBlank(column)) {
                    LOG.info("没有选座");
                } else {
                    // 判断column，有值的话就比对列号
                    if (!dailyTrainSeat.getCol().equals(column)) {
                        LOG.info("座位{}列值错误，继续找下一个座位，当前列值：{}，目标列值：{}", dailyTrainSeat.getCarriageSeatIndex(), column, dailyTrainSeat.getCol());
                        continue;
                    }

                }
                //第一个座位
                boolean fixSell = calSell(dailyTrainSeat, startIndex, endIndex);
                if (fixSell) {
                    LOG.info("选中座位");
                    getSeatList.add(dailyTrainSeat);
                } else {
                    continue;
                }

                //根据offsetList选剩下的座位
                boolean isGetAllOffset = true;//跳出外部循环，保证第二个座位选中

                if (CollUtil.isNotEmpty(offsetList)) {
                    LOG.info("有偏移值：{}，校验偏移的座位是否可选", offsetList);
                    //从索引1开始，索引0就是当前已选中的座位
                    for (int i = 1; i < offsetList.size(); i++) {
                        Integer offset = offsetList.get(i);
                        //座位和车厢在库中的操作是从索引1开始的，而trainSeatList.get(nextIndex)是从0开始的
                        int nextIndex = dailyTrainSeat.getCarriageSeatIndex() + offset - 1;

                        //有选座时，一定要在同一车厢中
                        if (nextIndex >= trainSeatList.size()) {
                            LOG.info("偏移后的座位索引:{}超出范围，不进行选座", nextIndex);
                            isGetAllOffset = false;
                            break;
                        }
                        DailyTrainSeat nextSeat = trainSeatList.get(nextIndex);
                        boolean fixSellNext = calSell(nextSeat, startIndex, endIndex);
                        if (fixSellNext) {
                            LOG.info("选中的下一个座位为:{}", nextSeat.getCarriageSeatIndex());
                            getSeatList.add(nextSeat);
                        } else {
                            LOG.info("座位:{}不可选", nextSeat.getCarriageSeatIndex());
                            isGetAllOffset = false;
                            break;
                        }
                    }
                }

                if (!isGetAllOffset) {
                    //清空getSeatList中的元素
                    CollUtil.clear(getSeatList);
                    continue;
                }

                //保存选好的座位
                finalSeatList.addAll(getSeatList);
                return;
            }
        }

    }

    /**
     * 计算座位销售情况
     * 比如说：sell=10001,本次购买的区间站为1~4，则区间已售000
     * 全是0，表示这个区间可买;只要有1，就表示区间内已售过票
     * <p>
     * 选中后要计算购票后的sell,比如原来的sell为10001，本次购买的区间站1~4
     * 方案：构造本次购票造成的售卖信息01110,和原来的sell按位或最终得到11111
     *
     * @param dailyTrainSeat
     */
    private boolean calSell(DailyTrainSeat dailyTrainSeat, Integer startIndex, Integer endIndex) {
        //10001 00000
        String sell = dailyTrainSeat.getSell();
        //000   000
        String sellPart = sell.substring(startIndex, endIndex);
        if (Integer.parseInt(sellPart) > 0) {
            LOG.warn("在区间{}~{}的{}号座位已售", startIndex, endIndex, dailyTrainSeat.getCarriageSeatIndex());
            return false;
        }

        LOG.warn("在区间{}~{}的{}号座位未售", startIndex, endIndex, dailyTrainSeat.getCarriageSeatIndex());
        //111    111
        String curSell = sellPart.replace('0', '1');
        //0111   0111,其中endIndex表示补完位后字符串的总长度
        curSell = StrUtil.fillBefore(curSell, '0', endIndex);
        //01110  01110
        curSell = StrUtil.fillAfter(curSell, '0', sell.length());

        int newSellInt = NumberUtil.binaryToInt(curSell) | NumberUtil.binaryToInt(sell);
        String newSell = NumberUtil.getBinaryStr(newSellInt);
        //为了防止提交到数据库时，第二种情况中01110中的第一个‘0’消失
        newSell = StrUtil.fillBefore(newSell, '0', sell.length());
        LOG.info("座位:{}被选中，原售票信息:{},车站区间:{}~{},最终售票信息为:{}", dailyTrainSeat.getCarriageSeatIndex(), sell, startIndex, endIndex, newSell);
        dailyTrainSeat.setSell(newSell);
        return true;

    }


    /**
     * 扣减余票
     *
     * @param req
     * @param dailyTrainTicket
     */
    private static void reduceTicket(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for (ConfirmOrderTicketReq ticket : req.getTickets()) {
            String seatTypeCode = ticket.getSeatTypeCode();
            SeatTypeEnum seatType = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            switch (seatType) {
                case YDZ -> {
                    int ydzTicketCount = dailyTrainTicket.getYdz() - 1;
                    if (ydzTicketCount < 0) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYdz(ydzTicketCount);
                }
                case EDZ -> {
                    int edzTicketCount = dailyTrainTicket.getEdz() - 1;
                    if (edzTicketCount < 0) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setEdz(edzTicketCount);
                }
                case YW -> {
                    Integer ywTicketCount = dailyTrainTicket.getYw() - 1;
                    if (ywTicketCount < 0) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYw(ywTicketCount);
                }
                case RW -> {
                    Integer rwTicketCount = dailyTrainTicket.getRw() - 1;
                    if (rwTicketCount < 0) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setRw(rwTicketCount);
                }
            }
        }
    }

    /**
     * 购票请求限流或降级处理逻辑(参数(要加上BlockException ex)和返回值保持一致)
     * 此方法没有生效的原因:BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_ERROR)异常，从而终止方法的继续执行。
     * 在这种情况下，由于异常是在分布式锁获取失败时抛出的，并不是由 Sentinel 限流或降级导致的异常，
     * 因此 Sentinel 不会触发blockHandler中定义的doConfirmBlockHandler方法
     * <p>
     * 限流或降级后的处理函数
     *
     * @param req
     * @param ex
     */
    public void doConfirmBlockHandler(ConfirmOrderDoReq req, BlockException ex) {
        LOG.warn("购票请求被限流或降级了!!!:{}", req);
        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_FAIL);
    }


}
