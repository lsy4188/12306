package itlsy.entry;

import lombok.Data;

import java.util.Date;

@Data
public class Member {
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String phone;

    private Integer userType;

    private Integer idType;

    private String idCard;

    private Long deletionTime;

    private Date createTime;

    private Date updateTime;
}