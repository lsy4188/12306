package itlsy.handler.select;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import itlsy.context.LoginMemberContext;
import itlsy.enums.VehicleSeatTypeEnum;
import itlsy.feign.dto.PassengerRespDTO;
import itlsy.dto.PurchaseTicketPassengerDetailDTO;
import itlsy.dto.SelectSeatDTO;
import itlsy.dto.TrainPurchaseTicketRespDTO;
import itlsy.entry.TrainStationPrice;
import itlsy.entry.TrainStationPriceExample;
import itlsy.enums.VehicleTypeEnum;
import itlsy.feign.UserRemoteService;
import itlsy.mapper.TrainStationPriceMapper;
import itlsy.req.PurchaseTicketReqDTO;
import itlsy.result.Result;
import itlsy.service.SeatService;
import itlsy.strategy.AbstractStrategyChoose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 购票时列车座位选择器
 */
//TODO 待完成
@Slf4j
@Component
public class TrainSeatTypeSelector {

    @Autowired
    private ThreadPoolExecutor selectSeatThreadPoolExecutor;

    @Autowired
    private AbstractStrategyChoose abstractStrategyChoose;

    @Autowired
    private UserRemoteService userRemoteService;

    @Autowired
    private TrainStationPriceMapper trainStationPriceMapper;

    @Autowired
    private SeatService seatService;

//TODO 待完善
    public List<TrainPurchaseTicketRespDTO> select(Integer trainType, PurchaseTicketReqDTO requestParam) {
        List<PurchaseTicketPassengerDetailDTO> passengerDetails = requestParam.getPassengers();
        // 按照座位类型进行分组
        Map<Integer, List<PurchaseTicketPassengerDetailDTO>> seatTypeMap = passengerDetails.stream()
                .collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType));

       List<TrainPurchaseTicketRespDTO> actualResult = new CopyOnWriteArrayList<>();
        // 如果座位类型映射的大小大于1采用线程池提高效率
        if (seatTypeMap.size()>1){
           // 创建一个用于存放Future对象的列表
           List<Future<List<TrainPurchaseTicketRespDTO>>> futureResults = new ArrayList<>();
           seatTypeMap.forEach((seatType,passengerSeatDetails)->{
               // 将每个座位类型的请求提交到线程池中执行
               Future<List<TrainPurchaseTicketRespDTO>> completableFuture = selectSeatThreadPoolExecutor
                       .submit(() -> distributeSeats(trainType, seatType, requestParam, passengerSeatDetails));
               // 将每个Future对象添加到列表中
               futureResults.add(completableFuture);
           });
           // 使用并行流parallelStream遍历Future对象列表提高遍历和处理futureResults列表的效率
           futureResults.parallelStream().forEach(completableFuture->{
               try {
                   // 将线程池执行任务的结果添加到实际结果中
                   actualResult.addAll(completableFuture.get());
               } catch (Exception e) {
                   throw new RuntimeException("站点余票不足，请尝试更换座位类型或选择其它站点");
               }
           });
       }else {
           seatTypeMap.forEach((seatType,passengerSeatDetails)->{
               List<TrainPurchaseTicketRespDTO> aggregationResult = distributeSeats(trainType, seatType, requestParam, passengerSeatDetails);
               actualResult.addAll(aggregationResult);
           });
       }
       if (CollUtil.isEmpty(actualResult)|| ObjectUtil.notEqual(actualResult.size(),passengerDetails.size())){
           throw new RuntimeException("站点余票不足，请尝试更换座位类型或选择其它站点");
       }
        List<String> passengerIds = actualResult.stream()
                .map(TrainPurchaseTicketRespDTO::getPassengerId)
                .toList();
        Result<List<PassengerRespDTO>> passengerRemoteResult;
        List<PassengerRespDTO> passengerRemoteResultList;
        passengerRemoteResult = userRemoteService.listPassengerQueryByIds(LoginMemberContext.getUserName(), passengerIds);
        if (!passengerRemoteResult.isSuccess()||CollUtil.isEmpty(passengerRemoteResultList = passengerRemoteResult.getData())){
           throw new RuntimeException("用户服务远程调用查询乘车人相信信息错误");
        }
        actualResult.forEach((item)->{
            String passengerId = item.getPassengerId();
            passengerRemoteResultList.stream()
                    .filter(each->ObjectUtil.equals(each.getId(),passengerId))
                    .findFirst()
                    .ifPresent(passenger->{
                        item.setIdCard(passenger.getIdCard());
                        item.setPhone(passenger.getPhone());
                        item.setUserType(passenger.getDiscountType());
                        item.setIdType(passenger.getIdType());
                        item.setRealName(passenger.getRealName());
                    });
            TrainStationPriceExample priceExample = new TrainStationPriceExample();
            priceExample.createCriteria()
                    .andTrainIdEqualTo(Long.valueOf(requestParam.getTrainId()))
                    .andDepartureEqualTo(requestParam.getDeparture())
                    .andArrivalEqualTo(requestParam.getArrival())
                    .andSeatTypeEqualTo(item.getSeatType());
            TrainStationPrice trainStationPrice = trainStationPriceMapper.selectByExample(priceExample).get(0);
            item.setAmount(trainStationPrice.getPrice());
        });
        // 购买列车中间站点余票更新
        seatService.lockSeat(requestParam.getTrainId(), requestParam.getDeparture(), requestParam.getArrival(), actualResult);
        return actualResult;
    }


    private List<TrainPurchaseTicketRespDTO> distributeSeats(Integer trainType, Integer seatType, PurchaseTicketReqDTO requestParam, List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails) {
        String buildStrategyKey = VehicleTypeEnum.findNameByCode(trainType) + VehicleSeatTypeEnum.findNameByCode(seatType);
        SelectSeatDTO selectSeatDTO = SelectSeatDTO.builder()
                .seatType(seatType)
                .passengerSeatDetails(passengerSeatDetails)
                .requestParam(requestParam)
                .build();
        try {
            return abstractStrategyChoose.chooseAndExecuteResp(buildStrategyKey, selectSeatDTO);
        } catch (Exception e) {
            throw new RuntimeException("当前车次列车类型暂未适配，请购买G35或G39车次");
        }
    }
}
