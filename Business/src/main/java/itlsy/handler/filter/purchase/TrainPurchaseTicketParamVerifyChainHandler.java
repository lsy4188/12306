package itlsy.handler.filter.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import itlsy.DistributedCache;
import itlsy.constant.Index12306Constant;
import itlsy.constant.RedisKeyConstant;
import itlsy.entry.Train;
import itlsy.entry.TrainStation;
import itlsy.entry.TrainStationExample;
import itlsy.exception.ClientException;
import itlsy.mapper.TrainMapper;
import itlsy.mapper.TrainStationMapper;
import itlsy.req.PurchaseTicketReqDTO;
import itlsy.util.EnvironmentUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 购票流程过滤器之验证参数是否有效
 * 验证参数有效这个流程会大量交互缓存，为了优化性能需要使用 Lua。为了方便理解流程，这里使用多次调用缓存
 */
@Component
@RequiredArgsConstructor
public class TrainPurchaseTicketParamVerifyChainHandler implements TrainPurchaseTicketChainFilter<PurchaseTicketReqDTO> {

    private final TrainMapper trainMapper;
    private final TrainStationMapper trainStationMapper01;
    private final DistributedCache distributedCache;

    @Override
    public void handler(PurchaseTicketReqDTO requestParam) {
        Train train = distributedCache.safeGet(
                RedisKeyConstant.TRAIN_INFO + requestParam.getTrainId(),
                Train.class,
                () -> trainMapper.selectByPrimaryKey(Long.valueOf(requestParam.getTrainId())),
                Index12306Constant.ADVANCE_TICKET_DAY,
                TimeUnit.DAYS
        );
        if (ObjectUtil.isNull(train)) {
            // 如果按照严谨逻辑，类似异常应该记录当前用户的 userid 并发送到风控中心
            // 如果一段时间有过几次的异常，直接封号处理。下述异常同理
            throw new ClientException("请检查车次是否存在");
        }
        // TODO，当前列车数据并没有通过定时任务每天生成最新的，所以需要隔离这个拦截。后期定时生成数据后删除该判断
        if (!EnvironmentUtil.isDevEnvironment()) {
            //查询车次是否已经发售
            if (new Date().before(train.getSaleTime())) {
                throw new ClientException("列车车次暂未发售");
            }
            //查询车次是否在有效期内
            if (new Date().after(train.getDepartureTime())) {
                throw new ClientException("列车车次已出发禁止购票");
            }
        }
        //车站是否存在车次中，以及车站顺序是否正确
        String trainStationStopoverDetailStr = distributedCache.safeGet(
                RedisKeyConstant.TRAIN_STATION_STOPOVER_DETAIL + requestParam.getTrainId(),
                String.class,
                () -> {
                    TrainStationExample example = new TrainStationExample();
                    example.createCriteria().andTrainIdEqualTo(Long.valueOf(requestParam.getTrainId()));
                    List<TrainStation> actualTrainStationList = trainStationMapper01.selectByExample(example);
                    return CollUtil.isNotEmpty(actualTrainStationList) ? JSONUtil.toJsonStr(actualTrainStationList) : null;
                },
                Index12306Constant.ADVANCE_TICKET_DAY,
                TimeUnit.DAYS
        );
        List<TrainStation> trainDOList = JSON.parseArray(trainStationStopoverDetailStr, TrainStation.class);
        boolean validateStation =validateStation(
                trainDOList.stream().map(TrainStation::getDeparture).toList(),
                requestParam.getDeparture(),
                requestParam.getArrival()
        );
        if (!validateStation) {
            throw new ClientException("列车车站数据错误");
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }

    private boolean validateStation(List<String> stationList, String departure, String arrival) {
        int startStationIndex = stationList.indexOf(departure);
        int endStationIndex = stationList.indexOf(arrival);
        if (startStationIndex==-1|| endStationIndex==-1){
            return false;
        }
        return startStationIndex<=endStationIndex;
    }
}
