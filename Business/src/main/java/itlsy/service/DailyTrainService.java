//package itlsy.service;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.date.DateTime;
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.core.util.ObjectUtil;
//import com.github.pagehelper.PageHelper;
//import com.github.pagehelper.PageInfo;
//import itlsy.entry.DailyTrain;
//import itlsy.entry.DailyTrainExample;
//import itlsy.entry.Train;
//import itlsy.mapper.DailyTrainMapper;
//import itlsy.req.DailyTrainQueryReq;
//import itlsy.req.DailyTrainSaveReq;
//import itlsy.resp.DailyTrainQueryResp;
//import itlsy.resp.PageResp;
//import itlsy.util.SnowUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Date;
//import java.util.List;
//
//@Service
//public class DailyTrainService {
//
//    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainService.class);
//
//    @Autowired
//    private DailyTrainMapper dailyTrainMapper;
//
//    @Autowired
//    private TrainService trainService;
//
//    @Autowired
//    private DailyTrainStationService dailyTrainStationService;
//
//    @Autowired
//    private DailyTrainCarriageService dailyTrainCarriageService;
//
//    @Autowired
//    private DailyTrainSeatService dailyTrainSeatService;
//
//    @Autowired
//    private DailyTrainTicketService dailyTrainTicketService;
//
//    @Autowired
//    private SkTokenService skTokenService;
//
//    public void save(DailyTrainSaveReq req) {
//        DateTime now = DateTime.now();
//        DailyTrain dailyTrain = BeanUtil.copyProperties(req, DailyTrain.class);
//        if (ObjectUtil.isNull(dailyTrain.getId())) {
//            dailyTrain.setId(SnowUtil.getSnowflakeNextId());
//            dailyTrain.setCreateTime(now);
//            dailyTrain.setUpdateTime(now);
//            dailyTrainMapper.insert(dailyTrain);
//        } else {
//            dailyTrain.setUpdateTime(now);
//            dailyTrainMapper.updateByPrimaryKey(dailyTrain);
//        }
//    }
//
//    public PageResp<DailyTrainQueryResp> queryList(DailyTrainQueryReq req) {
//        DailyTrainExample dailyTrainExample = new DailyTrainExample();
//        dailyTrainExample.setOrderByClause("date desc, code asc");
//        DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();
//        if (ObjectUtil.isNotNull(req.getDate())) {
//            criteria.andDateEqualTo(req.getDate());
//        }
//        if (ObjectUtil.isNotEmpty(req.getCode())) {
//            criteria.andCodeEqualTo(req.getCode());
//        }
//
//        LOG.info("查询页码：{}", req.getPage());
//        LOG.info("每页条数：{}", req.getSize());
//        PageHelper.startPage(req.getPage(), req.getSize());
//        List<DailyTrain> dailyTrainList = dailyTrainMapper.selectByExample(dailyTrainExample);
//
//        PageInfo<DailyTrain> pageInfo = new PageInfo<>(dailyTrainList);
//        LOG.info("总行数：{}", pageInfo.getTotal());
//        LOG.info("总页数：{}", pageInfo.getPages());
//
//        List<DailyTrainQueryResp> list = BeanUtil.copyToList(dailyTrainList, DailyTrainQueryResp.class);
//
//        PageResp<DailyTrainQueryResp> pageResp = new PageResp<>();
//        pageResp.setTotal(pageInfo.getTotal());
//        pageResp.setList(list);
//        return pageResp;
//    }
//
//
//    public void delete(Long id) {
//        dailyTrainMapper.deleteByPrimaryKey(id);
//    }
//
//    /**
//     * 生成某日所有车次信息，包括车次、车站、车厢、座位
//     * @param date
//     */
//    public void genDaily(Date date) {
//        List<Train> trainList = trainService.selectAll();
//        if (CollUtil.isEmpty(trainList)) {
//            LOG.info("没有车次基础数据，任务结束");
//            return;
//        }
//
//        for (Train train : trainList) {
//            genDailyTrain(date, train);
//        }
//    }
//
//    /**
//     * 将方法提取实现高内聚低耦合
//     * @param date
//     * @param train
//     */
//    @Transactional
//    public void genDailyTrain(Date date, Train train) {
//        LOG.info("生成日期【{}】车次【{}】的信息开始", DateUtil.formatDate(date), train.getCode());
//        // 删除该车次已有的数据
//        DailyTrainExample dailyTrainExample = new DailyTrainExample();
//        dailyTrainExample.createCriteria()
//                .andDateEqualTo(date)
//                .andCodeEqualTo(train.getCode());
//        dailyTrainMapper.deleteByExample(dailyTrainExample);
//
//        // 生成该车次的数据
//        DateTime now = DateTime.now();
//        DailyTrain dailyTrain = BeanUtil.copyProperties(train, DailyTrain.class);
//        dailyTrain.setId(SnowUtil.getSnowflakeNextId());
//        dailyTrain.setCreateTime(now);
//        dailyTrain.setUpdateTime(now);
//        dailyTrain.setDate(date);
//        dailyTrainMapper.insert(dailyTrain);
//
//        // 生成该车次的车站数据
//        dailyTrainStationService.genDaily(date, train.getCode());
//
//        // 生成该车次的车厢数据
//        dailyTrainCarriageService.genDaily(date, train.getCode());
//
//        // 生成该车次的座位数据
//        dailyTrainSeatService.genDaily(date, train.getCode());
//
//        // 生成该车次的余票数据
//        dailyTrainTicketService.genDaily(dailyTrain, date, train.getCode());
//
//        // 生成该车次的令牌数据
//        skTokenService.genDaily(date, train.getCode());
//
//        LOG.info("生成日期【{}】车次【{}】的信息结束", DateUtil.formatDate(date), train.getCode());
//    }
//
//    /**
//     * 删除2天之前的所有火车相关数据
//     */
//    public void deleteBefore2Days(Date date) {
//        List<Train> trains = trainService.selectAll();
//        if (CollUtil.isEmpty(trains)) {
//            LOG.info("没有车次基础数据，任务结束");
//            return;
//        }
//        for (Train train : trains) {
//            genDeleteBefore2Days(date, train);
//        }
//    }
//
//    @Transactional
//    public void genDeleteBefore2Days(Date date, Train train) {
//        // 删除该车次两天前的数据
//        DailyTrainExample dailyTrainExample = new DailyTrainExample();
//        dailyTrainExample.createCriteria().andDateLessThanOrEqualTo(date).andCodeEqualTo(train.getCode());
//        dailyTrainMapper.deleteByExample(dailyTrainExample);
//
//        //删除该车次两天前的车站数据
//        dailyTrainStationService.deleteBefore2Days(date, train.getCode());
//
//        //删除该车次两天前的车厢数据
//        dailyTrainCarriageService.deleteBefore2Days(date, train.getCode());
//
//        //删除该车次两天前的座位数据
//        dailyTrainSeatService.deleteBefore2Days(date, train.getCode());
//
//        //删除该车次两天前的余票数据
//        dailyTrainTicketService.deleteBefore2Days(date, train.getCode());
//
//        //删除该车次两天前的令牌数据
//        skTokenService.deleteBefore2Days(date, train.getCode());
//
//        LOG.info("删除日期【{}】车次【{}】两天前的数据结束", DateUtil.formatDate(date), train.getCode());
//    }
//}
