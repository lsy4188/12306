package itlsy.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MemberRegisterReq {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotNull(message = "证件类型不能为空")
    private Integer idType;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    //@Valid在controller类中开启NotBlank的功能
    @NotBlank(message = "【手机号】不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号码格式错误")
    //^1\d{10}$表示：以1开头，第二位是1-9的数字，后面是10位数字
    private String phone;


    private Integer userType;


}
