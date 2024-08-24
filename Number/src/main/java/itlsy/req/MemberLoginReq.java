package itlsy.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MemberLoginReq {

//    @NotBlank(message = "【手机号】不能为空")
//    @Pattern(regexp = "^1\\d{10}$", message = "手机号码格式错误")
//    //^1\d{10}$表示：以1开头，第二位是1-9的数字，后面是10位数字
//    private String mobile;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
