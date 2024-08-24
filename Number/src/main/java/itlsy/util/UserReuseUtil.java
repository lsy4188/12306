package itlsy.util;

import itlsy.constant.lsy12306Constant;

/**
 * 用户名可复用工具类
 */
public final class UserReuseUtil {
    /**
     * 计算分片位置
     */
    public static int hashShardingIdx(String username) {
        return Math.abs(username.hashCode() % lsy12306Constant.USER_REGISTER_REUSE_SHARDING_COUNT);
    }
}
