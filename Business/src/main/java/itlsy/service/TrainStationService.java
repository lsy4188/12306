package itlsy.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import itlsy.entry.TrainStation02;
import itlsy.entry.TrainStationExample02;
import itlsy.exception.BusinessException;
import itlsy.exception.BusinessExceptionEnum;
import itlsy.mapper.TrainStationMapper02;
import itlsy.req.TrainStationQueryReq;
import itlsy.req.TrainStationSaveReq;
import itlsy.resp.PageResp;
import itlsy.resp.TrainStationQueryResp;
import itlsy.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TrainStationService {

    @Autowired
    private TrainStationMapper02 trainStationMapper;

    public void save(TrainStationSaveReq req) {
        DateTime now = DateTime.now();
        TrainStation02 trainStation = BeanUtil.copyProperties(req, TrainStation02.class);
        if (ObjectUtil.isNull(trainStation.getId())) {

            // 保存之前，先校验唯一键是否存在
            TrainStation02 trainStationDB = selectByUnique(req.getTrainCode(), req.getIndex());
            if (ObjectUtil.isNotEmpty(trainStationDB)) {
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR);
            }
            // 保存之前，先校验唯一键是否存在
            trainStationDB = selectByUnique(req.getTrainCode(), req.getName());
            if (ObjectUtil.isNotEmpty(trainStationDB)) {
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR);
            }

            trainStation.setId(SnowUtil.getSnowflakeNextId());
            trainStation.setCreateTime(now);
            trainStation.setUpdateTime(now);
            trainStationMapper.insert(trainStation);
        } else {
            trainStation.setUpdateTime(now);
            trainStationMapper.updateByPrimaryKey(trainStation);
        }
    }

    private TrainStation02 selectByUnique(String trainCode, Integer index) {
        TrainStationExample02 trainStationExample = new TrainStationExample02();
        trainStationExample.createCriteria()
                .andTrainCodeEqualTo(trainCode)
                .andIndexEqualTo(index);
        List<TrainStation02> list = trainStationMapper.selectByExample(trainStationExample);
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0);
        } else {
            return null;
        }
    }

    private TrainStation02 selectByUnique(String trainCode, String name) {
        TrainStationExample02 trainStationExample = new TrainStationExample02();
        trainStationExample.createCriteria()
                .andTrainCodeEqualTo(trainCode)
                .andNameEqualTo(name);
        List<TrainStation02> list = trainStationMapper.selectByExample(trainStationExample);
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public PageResp<TrainStationQueryResp> queryList(TrainStationQueryReq req) {
        TrainStationExample02 trainStationExample = new TrainStationExample02();
        trainStationExample.setOrderByClause("train_code asc, `index` asc");
        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
           trainStationExample.createCriteria().andTrainCodeEqualTo(req.getTrainCode());
        }

        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainStation02> trainStationList = trainStationMapper.selectByExample(trainStationExample);

        PageInfo<TrainStation02> pageInfo = new PageInfo<>(trainStationList);
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        List<TrainStationQueryResp> list = BeanUtil.copyToList(trainStationList, TrainStationQueryResp.class);

        PageResp<TrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainStationMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据车次查询该车次经过的所有车站信息
     * @param trainCode
     * @return
     */
    public List<TrainStation02> selectByTrainCode(String trainCode) {
        TrainStationExample02 trainStationExample = new TrainStationExample02();
        trainStationExample.setOrderByClause("`index` asc");
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode);
        return trainStationMapper.selectByExample(trainStationExample);
    }
}
