package itlsy.entry;

import lombok.Data;

import java.util.Date;

@Data
public class Passengers {
    private Long id;

    private String username;

    private String realName;

    private Integer idType;

    private String idCard;

    private Integer discountType;

    private String phone;

    private Date createTime;

    private Date updateTime;

    private Boolean delFlag;
}