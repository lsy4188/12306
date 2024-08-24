package itlsy.req;

import lombok.Data;

@Data
public class UserLoginReq {

    /**
     * 用户名
     */
    private String usernameOrMailOrPhone;

    /**
     * 密码
     */
    private String password;
}
