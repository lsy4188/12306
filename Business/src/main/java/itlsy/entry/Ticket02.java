package itlsy.entry;

import lombok.Data;

import java.util.Date;
@Data
public class Ticket02 {
    private Long id;

    private Long numberId;

    private Long passengerId;

    private String passengerName;

    private Date trainDate;

    private String trainCode;

    private Integer carriageIndex;

    private String seatRow;

    private String seatCol;

    private String startStation;

    private Date startTime;

    private String endStation;

    private Date endTime;

    private String seatType;

    private Date createTime;

    private Date updateTime;


}