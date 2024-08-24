package itlsy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import itlsy.dto.RouteDTO;
import itlsy.entry.*;
import itlsy.mapper.StationMapper;
import itlsy.mapper.TrainStationMapper;
import itlsy.req.*;
import itlsy.resp.*;
import itlsy.service.StationService;
import itlsy.util.StationCalculateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationServiceImpl implements StationService {

    private static final Logger LOG = LoggerFactory.getLogger(StationServiceImpl.class);

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private TrainStationMapper trainStationMapper;

    public PageResp<StationQueryResp> queryList(StationQueryReq req) {
        StationExample stationExample = new StationExample();
        stationExample.setOrderByClause("id desc");
        StationExample.Criteria criteria = stationExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Station> stationList = stationMapper.selectByExample(stationExample);

        PageInfo<Station> pageInfo = new PageInfo<>(stationList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<StationQueryResp> list = BeanUtil.copyToList(stationList, StationQueryResp.class);

        PageResp<StationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        stationMapper.deleteByPrimaryKey(id);
    }

    public List<StationQueryRespDTO> queryAll() {
        StationExample stationExample = new StationExample();
        List<Station> stationList = stationMapper.selectByExample(stationExample);
        return BeanUtil.copyToList(stationList, StationQueryRespDTO.class);
    }

    @Override
    public List<TrainStationQueryRespDTO> listTrainStationQuery(String trainId) {
        TrainStationExample trainStationExample01 = new TrainStationExample();
        trainStationExample01.createCriteria()
                .andTrainIdEqualTo(Long.valueOf(trainId));
        List<TrainStation> trainStation01s = trainStationMapper.selectByExample(trainStationExample01);
        return BeanUtil.copyToList(trainStation01s, TrainStationQueryRespDTO.class);
    }

    @Override
    public List<RouteDTO> listTrainStationRoute(String trainId, String startStation, String endStation) {
        TrainStationExample trainStationExample01 = new TrainStationExample();
        trainStationExample01.createCriteria().andTrainIdEqualTo(Long.valueOf(trainId));
        List<TrainStation> trainStationDOList = trainStationMapper.selectByExample(trainStationExample01);
        List<String> trainStationAllList = trainStationDOList.stream().map(TrainStation::getDeparture).toList();
        return StationCalculateUtil.throughStation(trainStationAllList, startStation, endStation);
    }

    @Override
    public List<RouteDTO> listTakeoutTrainStationRoute(String trainId, String departure, String arrival) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.createCriteria()
                .andTrainIdEqualTo(Long.valueOf(trainId));
        List<TrainStation> trainStationDOList = trainStationMapper.selectByExample(trainStationExample);
        List<String> trainStationAllList = trainStationDOList.stream().map(TrainStation::getDeparture).toList();
        return StationCalculateUtil.takeoutStation(trainStationAllList, departure, arrival);
    }
}
