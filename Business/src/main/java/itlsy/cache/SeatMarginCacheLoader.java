package itlsy.cache;

import itlsy.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 座位余量缓存加载
 */
@Component
@RequiredArgsConstructor
public class SeatMarginCacheLoader {


    public Map<String, String> load(String trainId, String seatType, String departure, String arrival) {
        LinkedHashMap<String, Map<String, String>> trainStationRemainingTicketMaps = new LinkedHashMap<>();
        String keySuffix = CacheUtil.buildKey(trainId, departure, arrival);
        return null;
    }

}
