package itlsy.handler.filter.query;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import itlsy.DistributedCache;
import itlsy.constant.RedisKeyConstant;
import itlsy.req.TicketPageQueryReqDTO;
import itlsy.entry.*;
import itlsy.exception.ClientException;
import itlsy.mapper.RegionMapper;
import itlsy.mapper.StationMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * 查询列车车票流程过滤器之验证数据是否正确
 */
@Component
@RequiredArgsConstructor
public class TrainTicketQueryParamVerifyChainFilter implements TrainTicketQueryChainFilter<TicketPageQueryReqDTO> {

    private final RegionMapper regionMapper;
    private final StationMapper stationMapper;
    private final DistributedCache distributedCache;
    private final RedissonClient redissonClient;

    /**
     * 缓存数据为空并且已经加载过标识
     */
    private static boolean CACHE_DATA_ISNULL_AND_LOAD_FLAG = false;

    @Override
    public void handler(TicketPageQueryReqDTO requestParam) {
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        List<Object> actualExistList = hashOperations.multiGet(
                RedisKeyConstant.QUERY_ALL_REGION_LIST,
                ListUtil.toList(requestParam.getFromStation(), requestParam.getToStation())
        );
        long emptyCount = actualExistList.stream().filter(Objects::isNull).count();
        if (emptyCount == 0L) {
            return;
        }
        if (emptyCount == 1L || (emptyCount == 2L && CACHE_DATA_ISNULL_AND_LOAD_FLAG && distributedCache.hasKey(RedisKeyConstant.QUERY_ALL_REGION_LIST))) {
            throw new ClientException("出发地或目的地不存在");
        }
        RLock lock = redissonClient.getLock(RedisKeyConstant.LOCK_QUERY_ALL_REGION_LIST);
        lock.lock();
        try {
            if (distributedCache.hasKey(RedisKeyConstant.QUERY_ALL_REGION_LIST)) {
                actualExistList = hashOperations.multiGet(
                        RedisKeyConstant.QUERY_ALL_REGION_LIST,
                        ListUtil.toList(requestParam.getFromStation(), requestParam.getToStation())
                );
                emptyCount = actualExistList.stream().filter(Objects::isNull).count();
                if (emptyCount != 2L) {
                    throw new ClientException("出发地或目的地不存在");
                }
                return;
            }
            List<Region> regionList = regionMapper.selectByExample(new RegionExample());
            List<Station> stationList = stationMapper.selectByExample(new StationExample());
            HashMap<Object, Object> regionValueMap = Maps.newHashMap();
            for (Region region : regionList) {
                regionValueMap.put(region.getCode(), region.getName());
            }
            for (Station station : stationList) {
                regionValueMap.put(station.getCode(), station.getName());
            }
            hashOperations.putAll(RedisKeyConstant.QUERY_ALL_REGION_LIST, regionValueMap);
            CACHE_DATA_ISNULL_AND_LOAD_FLAG = true;
            emptyCount = regionValueMap.keySet().stream()
                    .filter(each -> StrUtil.equalsAny(each.toString(), requestParam.getFromStation(), requestParam.getToStation()))
                    .count();
            if (emptyCount != 2L) {
                throw new ClientException("出发地或目的地不存在");
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
