package itlsy.entry;

import lombok.Data;

import java.util.Date;

/**
 * 旧版本
 */
@Data
public class TrainSeat02 {
    private Long id;

    private String trainCode;

    private Integer carriageIndex;

    private String row;

    private String col;

    private String seatType;

    private Integer carriageSeatIndex;

    private Date createTime;

    private Date updateTime;


}