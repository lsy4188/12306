package itlsy.req;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import itlsy.serialize.IdCardSerializer;
import itlsy.serialize.PhoneSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class PassengersSaveReq {
    /**
     * 乘车人id
     */
    private Long id;
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
    private String idCard;

    /**
     * 优惠类型
     */
    private Integer discountType;
    /**
     * 手机号
     */
    @NotBlank(message = "【手机号】不能为空")
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

}