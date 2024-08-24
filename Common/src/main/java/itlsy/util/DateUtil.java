package itlsy.util;

import cn.hutool.core.date.LocalDateTimeUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 日期工具类
 */
public final class DateUtil {
    /**
     * 计算小时差
     *
     * @param startTime 开始时间 2022-10-01 00:00:00
     * @param endTime   结束时间 2022-10-01 12:23:00
     * @return 12:23
     */
    public static String calculateHourDifference(Date startTime, Date endTime) {
        LocalDateTime startDateTime = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endDateTime = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Duration duration = Duration.between(startDateTime, endDateTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    /**
     * 将Date对象转换为LocalDateTime对象，并按照指定的格式进行格式化
     * @param date    时间
     * @param pattern 日期格式
     * @return 日期格式对应的时间
     */

    public static String convertDateToLocalTime(Date date,String pattern){
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return LocalDateTimeUtil.format(localDateTime,pattern);
    }
}
