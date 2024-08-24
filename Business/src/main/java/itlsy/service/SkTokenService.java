package itlsy.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import itlsy.entry.SkToken;
import itlsy.entry.SkTokenExample;
import itlsy.enums.RedisKeyPreEnum;
import itlsy.mapper.SkTokenMapper;
import itlsy.mapper.cust.SkTokenMapperCust;
import itlsy.req.SkTokenQueryReq;
import itlsy.req.SkTokenSaveReq;
import itlsy.resp.PageResp;
import itlsy.resp.SkTokenQueryResp;
import itlsy.util.SnowUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SkTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(SkTokenService.class);

    @Autowired
    private SkTokenMapper skTokenMapper;

    @Autowired
    private DailyTrainSeatService dailyTrainSeatService;

//    @Autowired
//    private DailyTrainStationService dailyTrainStationService;

    @Autowired
    private SkTokenMapperCust skTokenMapperCust;

    @Autowired
    private RedisTemplate redisTemplate;


    @Value("${spring.profiles.active}")
    private String env;

    /**
     * 初始化令牌信息
     */
    @Transactional
    public void genDaily(Date date, String trainCode) {
        LOG.info("删除日期【{}】车次【{}】的令牌记录", DateUtil.formatDate(date), trainCode);
        //首先删除库里面现有的
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        skTokenMapper.deleteByExample(skTokenExample);

        DateTime now = DateTime.now();
        SkToken skToken = new SkToken();
        skToken.setDate(date);
        skToken.setTrainCode(trainCode);
        skToken.setId(SnowUtil.getSnowflakeNextId());
        skToken.setCreateTime(now);
        skToken.setUpdateTime(now);

        //再通过构造信息重新查询并回填
        int seatCount = dailyTrainSeatService.countSeat(date, trainCode);
        LOG.info("车次【{}】座位数：{}", trainCode, seatCount);

//        long stationCount = dailyTrainStationService.countByTrainCode(date, trainCode);
        long stationCount =9L;
        LOG.info("车次【{}】到站数：{}", trainCode, stationCount);

        // 3/4需要根据实际卖票比例来定(不可能全部卖完)，一趟火车最多可以卖（seatCount * stationCount）张火车票
        int count = (int) (seatCount * stationCount); // * 3/4);
        LOG.info("车次【{}】初始生成令牌数：{}", trainCode, count);
        skToken.setCount(count);

        skTokenMapper.insert(skToken);
    }

    /**
     * 删除2天前的令牌信息
     * @param date
     * @param trainCode
     */
    public void deleteBefore2Days(Date date, String trainCode) {
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.createCriteria().andDateLessThanOrEqualTo(date).andTrainCodeEqualTo(trainCode);
        skTokenMapper.deleteByExample(skTokenExample);
    }

    public void save(SkTokenSaveReq req) {
        DateTime now = DateTime.now();
        SkToken skToken = BeanUtil.copyProperties(req, SkToken.class);
        if (ObjectUtil.isNull(skToken.getId())) {
            skToken.setId(SnowUtil.getSnowflakeNextId());
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            skTokenMapper.insert(skToken);
        } else {
            skToken.setUpdateTime(now);
            skTokenMapper.updateByPrimaryKey(skToken);
        }
    }

    public PageResp<SkTokenQueryResp> queryList(SkTokenQueryReq req) {
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.setOrderByClause("id desc");
        SkTokenExample.Criteria criteria = skTokenExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<SkToken> skTokenList = skTokenMapper.selectByExample(skTokenExample);

        PageInfo<SkToken> pageInfo = new PageInfo<>(skTokenList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<SkTokenQueryResp> list = BeanUtil.copyToList(skTokenList, SkTokenQueryResp.class);

        PageResp<SkTokenQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        skTokenMapper.deleteByPrimaryKey(id);
    }

    /**
     * 校验令牌
     */
    public boolean validSkToken(Date date, String trainCode, Long memberId) {
        LOG.info("会员【{}】获取日期【{}】车次【{}】的令牌开始", memberId, DateUtil.formatDate(date), trainCode);
        String skTokenCountKey = RedisKeyPreEnum.SK_TOKEN_COUNT + "-" + DateUtil.formatDate(date) + "-" + trainCode;
        Object skTokenCount = redisTemplate.opsForValue().get(skTokenCountKey);
        if (skTokenCount != null) {
            LOG.info("缓存中有该车次令牌大闸的key：{}", skTokenCountKey);
            Integer count = (Integer) redisTemplate.opsForValue().get(skTokenCountKey);
            LOG.info("获取令牌前，令牌余数：{}", count);
            redisTemplate.opsForValue().set(skTokenCountKey, count - 1);
            if (count < 0L) {
                LOG.error("获取令牌失败：{}", skTokenCountKey);
                return false;
            } else {
                LOG.info("获取令牌后，令牌余数：{}", count);
                redisTemplate.expire(skTokenCountKey, 30, TimeUnit.SECONDS);
                // 每获取5个令牌更新一次数据库(这个只适合于1分钟内有5次令牌的获取情况，如果1分钟没有5次令牌的获取情况，那么这个方法就失效了)
                if (count % 5 == 0) {
                    skTokenMapperCust.decrease(date, trainCode, 5);
                }
                return true;
            }
        } else {
            LOG.info("缓存中没有该车次令牌大闸的key：{}", skTokenCountKey);
            SkTokenExample skTokenExample = new SkTokenExample();
            skTokenExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
            List<SkToken> tokenCountList = skTokenMapper.selectByExample(skTokenExample);
            if (CollUtil.isEmpty(tokenCountList)) {
                LOG.info("找不到日期【{}】车次【{}】的令牌记录", DateUtil.formatDate(date), trainCode);
                return false;
            }
            SkToken skToken = tokenCountList.get(0);
            if (skToken.getCount() <= 0) {
                LOG.info("日期【{}】车次【{}】的令牌余量为0", DateUtil.formatDate(date), trainCode);
                return false;
            }
            Integer count = skToken.getCount() - 1;
            skToken.setCount(count);
            LOG.info("将该车次令牌大闸放入缓存中，key: {}， count: {}", skTokenCountKey, count);
            redisTemplate.opsForValue().set(skTokenCountKey, count, 60, TimeUnit.SECONDS);
            return true;
        }
    }


}
