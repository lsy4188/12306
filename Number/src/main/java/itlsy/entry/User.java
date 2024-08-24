package itlsy.entry;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String region;

    private Integer idType;

    private String idCard;

    private String phone;

    private String telephone;

    private String mail;

    private Integer userType;

    private Integer verifyStatus;

    private String postCode;

    private String address;

    private Long deletionTime;

    private Date createTime;

    private Date updateTime;

    private Boolean delFlag;

}