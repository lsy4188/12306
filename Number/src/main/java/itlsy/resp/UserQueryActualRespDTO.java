package itlsy.resp;

import lombok.Data;

/**
 * 用户查询返回无脱敏参数
 */
@Data
public class UserQueryActualRespDTO {

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
    private String idCard;

    /**
     * 手机号
     */
    private String phone;


    /**
     * 旅客类型
     */
    private Integer userType;

}
