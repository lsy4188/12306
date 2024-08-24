package itlsy.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import itlsy.serialize.IdCardSerializer;
import itlsy.serialize.PhoneSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class PassengersQueryResp {
    /**
     *乘车人id
     */
    @JsonSerialize(using = ToStringSerializer.class)//解决精度丢失问题
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 证件类型
     */
    private Integer idType;

    /**
     * 证件号码
     */
    @JsonSerialize(using = IdCardSerializer.class)
    private String idCard;

    /**
     * 真实证件号码
     */
    private String actualIdCard;

    /**
     * 优惠类型
     */
    private Integer discountType;
    /**
     * 手机号
     */
    @JsonSerialize(using = PhoneSerializer.class)
    private String phone;

    /**
     * 真实手机号
     */
    private String actualPhone;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

}