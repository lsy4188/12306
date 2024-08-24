package itlsy.resp;

import cn.crane4j.annotation.AssembleEnum;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import itlsy.enums.OrderItemStatusEnum;
import itlsy.serialize.IdCardSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车票订单详情返回参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketOrderPassengerDetailRespDTO {
    /**
     * ID
     */
    private String id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 席别类型
     */
    private Integer seatType;

    /**
     * 车厢号
     */
    private String carriageNumber;

    /**
     * 座位号
     */
    private String seatNumber;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 证件类型
     */
    private Integer idType;

    /**
     * 证件号
     */
    @JsonSerialize(using = IdCardSerializer.class)
    private String idCard;

    /**
     * 车票类型 0：成人 1：儿童 2：学生 3：残疾军人
     */
    private Integer ticketType;

    /**
     * 订单金额
     */
    private Integer amount;

    /**
     * 车票状态
     * @AssembleEnum:
     * 将枚举类型与一个变量关联起来，用来将status属性作为参数传递给statusName变量的。
     */
    @AssembleEnum(type = OrderItemStatusEnum.class, ref = "statusName")
    private Integer status;

    /**
     * 车票状态名称
     */
    private String statusName;
}
