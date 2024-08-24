package itlsy.util;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 缓存工具类
 */
public final class CacheUtil {

    private static final String SPLICING_OPERATOR = "_";


    public static String buildKey(String... keys){
        //Strings.emptyToNull(item)判断是否为空
        Stream.of(keys).forEach(item-> Optional.ofNullable(Strings.emptyToNull(item)).orElseThrow(()->new RuntimeException("构建缓存 key 不允许为空")));
        return Joiner.on(SPLICING_OPERATOR).join(keys);
    }

    /**
     * 判断结果是否为空或空的字符串
     *
     * @param cacheVal
     * @return
     */
    public static boolean isNullOrBlank(Object cacheVal){
        return cacheVal == null || (cacheVal instanceof String && Strings.isNullOrEmpty((String)cacheVal));
    }
}
