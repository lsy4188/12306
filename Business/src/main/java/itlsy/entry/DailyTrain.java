package itlsy.entry;

import lombok.Data;

import java.util.Date;

@Data
public class DailyTrain {
    private Long id;

    private Date date;

    private String code;

    private String type;

    private String start;

    private String startPinyin;

    private Date startTime;

    private String end;

    private String endPinyin;

    private Date endTime;

    private Date createTime;

    private Date updateTime;


}