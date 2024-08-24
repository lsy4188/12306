package itlsy.handler.base;

import itlsy.dto.SelectSeatDTO;
import itlsy.dto.TrainPurchaseTicketRespDTO;
import itlsy.dto.TrainSeatBaseDTO;
import itlsy.strategy.AbstractExecuteStrategy;

import java.util.List;

/**
 * 抽象高铁购票模板基础服务
 * TODO 待修改
 */
public abstract class AbstractTrainPurchaseTicketTemplate implements  AbstractExecuteStrategy<SelectSeatDTO, List<TrainPurchaseTicketRespDTO>> {
    /**
     * 选择座位
     *
     * @param requestParam 购票请求入参
     * @return 乘车人座位
     */
    protected abstract List<TrainPurchaseTicketRespDTO> selectSeats(SelectSeatDTO requestParam);

    protected TrainSeatBaseDTO buildTrainSeatBaseDTO(SelectSeatDTO requestParam){
       return TrainSeatBaseDTO.builder()
                .trainId(requestParam.getRequestParam().getTrainId())
                .departure(requestParam.getRequestParam().getDeparture())
                .arrival(requestParam.getRequestParam().getArrival())
                .chooseSeatList(requestParam.getRequestParam().getChooseSeats())
                .passengerSeatDetails(requestParam.getPassengerSeatDetails())
                .build();
    }

    @Override
    public List<TrainPurchaseTicketRespDTO> executeResp(SelectSeatDTO requestParam) {
        List<TrainPurchaseTicketRespDTO> actualResult = selectSeats(requestParam);
        //TODO 缺少扣减车厢余票缓存，扣减站点余票缓存
        return actualResult;
    }

}
