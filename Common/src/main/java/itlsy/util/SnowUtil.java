package itlsy.util;

import cn.hutool.core.util.IdUtil;

/**
 * 封装hutool雪花算法（总共64位，1个符号位+41个时间戳+10个工作机器id(包含数据中心和机器标识)+12个序列号id）
 */
public class SnowUtil {

    private static long dataCenterId = 1;  //数据中心(类似于机房)
    private static long workerId = 1;     //机器标识(类似于机房的机器)

    /**
     * 获取雪花算法生成的id
     * @return
     */
    public static long getSnowflakeNextId() {
        return IdUtil.getSnowflake(workerId, dataCenterId).nextId();
    }

    /**
     *  获取雪花算法生成的字符串id
     * @return
     */
    public static String getSnowflakeNextIdStr() {
        return IdUtil.getSnowflake(workerId, dataCenterId).nextIdStr();
    }
}
