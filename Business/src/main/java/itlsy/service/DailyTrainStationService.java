package itlsy.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import itlsy.entry.DailyTrainStation;
import itlsy.entry.DailyTrainStationExample;
import itlsy.entry.TrainStation02;
import itlsy.mapper.DailyTrainStationMapper;
import itlsy.req.DailyTrainStationQueryAllReq;
import itlsy.req.DailyTrainStationQueryReq;
import itlsy.req.DailyTrainStationSaveReq;
import itlsy.resp.DailyTrainStationQueryResp;
import itlsy.resp.PageResp;
import itlsy.util.SnowUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainStationService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainStationService.class);

    @Autowired
    private DailyTrainStationMapper dailyTrainStationMapper;

    @Autowired
    private TrainStationService trainStationService;

    public void save(DailyTrainStationSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(req, DailyTrainStation.class);
        if (ObjectUtil.isNull(dailyTrainStation.getId())) {
            dailyTrainStation.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.insert(dailyTrainStation);
        } else {
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.updateByPrimaryKey(dailyTrainStation);
        }
    }

    public PageResp<DailyTrainStationQueryResp> queryList(DailyTrainStationQueryReq req) {

        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        // 设置排序
        dailyTrainStationExample.setOrderByClause("date desc, train_code asc, `index` asc");
        // 创建查询条件
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
        // 判断日期是否为空
        if (ObjUtil.isNotNull(req.getDate())) {
            // 设置日期等于查询参数
            criteria.andDateEqualTo(req.getDate());
        }
        // 判断列车号是否为空
        if (ObjUtil.isNotEmpty(req.getTrainCode())) {
            // 设置列车号等于查询参数
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        // 调用PageHelper分页查询
        PageHelper.startPage(req.getPage(), req.getSize());
        // 执行查询
        List<DailyTrainStation> dailyTrainStationList = dailyTrainStationMapper.selectByExample(dailyTrainStationExample);

        // 获取分页信息
        PageInfo<DailyTrainStation> pageInfo = new PageInfo<>(dailyTrainStationList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        // 转换查询结果
        List<DailyTrainStationQueryResp> list = BeanUtil.copyToList(dailyTrainStationList, DailyTrainStationQueryResp.class);

        // 封装返回结果
        PageResp<DailyTrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainStationMapper.deleteByPrimaryKey(id);
    }

    @Transactional
    public void genDaily(Date date, String trainCode) {
        LOG.info("生成日期【{}】车次【{}】的车站信息开始", DateUtil.formatDate(date), trainCode);

        // 删除某日某车次的车站信息
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        dailyTrainStationMapper.deleteByExample(dailyTrainStationExample);

        // 查出某车次的所有的车站信息
        List<TrainStation02> stationList = trainStationService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(stationList)) {
            LOG.info("该车次没有车站基础数据，生成该车次的车站信息结束");
            return;
        }

        for (TrainStation02 trainStation : stationList) {
            DateTime now = DateTime.now();
            DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(trainStation, DailyTrainStation.class);
            dailyTrainStation.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStation.setDate(date);
            dailyTrainStationMapper.insert(dailyTrainStation);
        }
        LOG.info("生成日期【{}】车次【{}】的车站信息结束", DateUtil.formatDate(date), trainCode);
    }

    /**
     * 查询某日某车次的全部车站
     * @param date
     * @param trainCode
     * @return
     */
    public long countByTrainCode(Date date,String trainCode){
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        long stationCount = dailyTrainStationMapper.countByExample(dailyTrainStationExample);
        return stationCount;
    }

    /**
     * 查询某日某车次的全部车站
     * @param req
     * @return
     */
    public List<DailyTrainStationQueryResp> queryByTrain(DailyTrainStationQueryAllReq req) {
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.setOrderByClause("index asc");
        dailyTrainStationExample.createCriteria().andDateEqualTo(req.getDate()).andTrainCodeEqualTo(req.getTrainCode());
        List<DailyTrainStation> list = dailyTrainStationMapper.selectByExample(dailyTrainStationExample);
        return BeanUtil.copyToList(list,DailyTrainStationQueryResp.class);
    }

    /**
     * 删除某日之前某车次的车站信息
     * @param date
     * @param trainCode
     */
    public void deleteBefore2Days(Date date, String trainCode) {
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.createCriteria().andDateLessThanOrEqualTo(date).andTrainCodeEqualTo(trainCode);
        dailyTrainStationMapper.deleteByExample(dailyTrainStationExample);
    }
}
