package itlsy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import itlsy.DistributedCache;
import itlsy.constant.RedisKeyConstant;
import itlsy.dto.RouteDTO;
import itlsy.dto.SeatTypeCountDTO;
import itlsy.dto.TrainPurchaseTicketRespDTO;
import itlsy.entry.*;
import itlsy.enums.SeatStatusEnum;
import itlsy.mapper.TrainSeatMapper;
import itlsy.mapper.TrainStationMapper;
import itlsy.service.SeatService;
import itlsy.service.StationService;
import itlsy.util.StationCalculateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private StationService stationService;
    @Autowired
    private TrainStationMapper trainStationMapper01;
    @Autowired
    private TrainSeatMapper trainSeat01Mapper;
    @Autowired
    private DistributedCache distributedCache;

    @Override
    public void lockSeat(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketRespList) {
        List<RouteDTO> routeList = stationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
        trainPurchaseTicketRespList.forEach(item -> routeList.forEach(each -> {
            TrainSeatExample example = new TrainSeatExample();
            example.createCriteria()
                    .andTrainIdEqualTo(Long.valueOf(trainId))
                    .andCarriageNumberEqualTo(item.getCarriageNumber())
                    .andStartStationEqualTo(each.getStartStation())
                    .andEndStationEqualTo(each.getEndStation())
                    .andSeatNumberIn(Collections.singletonList(item.getSeatNumber()));
            TrainSeat build = TrainSeat.builder()
                    .seatStatus(SeatStatusEnum.LOCKED.getCode())
                    .build();
            trainSeat01Mapper.updateByExampleSelective(build, example);
        }));
    }

    @Override
    public List<String> listUsableCarriageNumber(String trainId, String departure, String arrival, Integer seatType) {
        TrainSeatExample example = new TrainSeatExample();
        example.createCriteria()
                .andTrainIdEqualTo(Long.valueOf(trainId))
                .andStartStationEqualTo(departure)
                .andEndStationEqualTo(arrival)
                .andSeatTypeEqualTo(seatType)
                .andSeatStatusEqualTo(SeatStatusEnum.AVAILABLE.getCode());
        List<TrainSeat> trainSeat01s = trainSeat01Mapper.selectByExample(example);
        return trainSeat01s.stream().map(TrainSeat::getCarriageNumber).toList();
    }

    @Override
    public List<Integer> listSeatRemainingTicket(String trainId, String departure, String arrival, List<String> trainCarriageList) {
        String keySuffix = StrUtil.join("_", trainId, departure, arrival);
        if (distributedCache.hasKey(RedisKeyConstant.TRAIN_STATION_CARRIAGE_REMAINING_TICKET + keySuffix)){
            StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
            List<Object> trainStationCarriageRemainingTicket = stringRedisTemplate.opsForHash().multiGet(RedisKeyConstant.TRAIN_STATION_CARRIAGE_REMAINING_TICKET + keySuffix, Arrays.asList(trainCarriageList.toArray()));
            if (CollUtil.isNotEmpty(trainStationCarriageRemainingTicket)){
                return trainStationCarriageRemainingTicket.stream().map(item->Integer.parseInt(item.toString())).collect(Collectors.toList());
            }
        }
        TrainSeat trainSeat = TrainSeat.builder()
                .trainId(Long.valueOf(trainId))
                .startStation(departure)
                .endStation(arrival)
                .build();
        return trainSeat01Mapper.listSeatRemainingTicket(trainSeat, trainCarriageList);
    }

    @Override
    public List<String> listAvailableSeat(String trainId, String carriageNumber, Integer seatType, String departure, String arrival) {
        TrainSeatExample example = new TrainSeatExample();
        example.createCriteria()
                .andTrainIdEqualTo(Long.valueOf(trainId))
                .andCarriageNumberEqualTo(carriageNumber)
                .andSeatTypeEqualTo(seatType)
                .andStartStationEqualTo(departure)
                .andEndStationEqualTo(arrival);
        List<TrainSeat> trainSeat = trainSeat01Mapper.selectByExample(example);
        return trainSeat.stream().map(TrainSeat::getSeatNumber).toList();
    }

    @Override
    public void unlock(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults) {
        List<RouteDTO> routeList = stationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
        trainPurchaseTicketResults.forEach(each -> routeList.forEach(item -> {
            TrainSeatExample example = new TrainSeatExample();
            example.createCriteria()
                    .andTrainIdEqualTo(Long.valueOf(trainId))
                    .andCarriageNumberEqualTo(each.getCarriageNumber())
                    .andStartStationEqualTo(item.getStartStation())
                    .andEndStationEqualTo(item.getEndStation())
                    .andSeatNumberEqualTo(each.getSeatNumber());
            TrainSeat updateTrainSeat = TrainSeat.builder()
                    .seatStatus(SeatStatusEnum.AVAILABLE.getCode())
                    .build();
            trainSeat01Mapper.updateByExampleSelective(updateTrainSeat, example);
        }));
    }

    @Override
    public List<SeatTypeCountDTO> listSeatTypeCount(Long trainId, String startStation, String endStation, List<Integer> seatTypes) {
        return trainSeat01Mapper.listSeatTypeCount(trainId, startStation, endStation, seatTypes);
    }

    @Override
    public List<RouteDTO> listTakeoutTrainStationRoute(String trainId, String departure, String arrival) {
        TrainStationExample example = new TrainStationExample();
        example.createCriteria().andTrainIdEqualTo(Long.valueOf(trainId));
        List<TrainStation> trainStationDOList = trainStationMapper01.selectByExample(example);
        List<String> trainStationAllList = trainStationDOList.stream().map(TrainStation::getDeparture).collect(Collectors.toList());
        return StationCalculateUtil.takeoutStation(trainStationAllList,departure,arrival);
    }
}
