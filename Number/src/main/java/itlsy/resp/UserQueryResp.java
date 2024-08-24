package itlsy.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import itlsy.serialize.IdCardSerializer;
import itlsy.serialize.PhoneSerializer;
import lombok.Data;

@Data
public class UserQueryResp {
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
     * 证件号
     */
    @JsonSerialize(using = IdCardSerializer.class)
    private String idCard;

    /**
     * 手机号
     */
    @JsonSerialize(using = PhoneSerializer.class)
    private String phone;

    /**
     * 旅客类型
     */
    private Integer userType;
}
