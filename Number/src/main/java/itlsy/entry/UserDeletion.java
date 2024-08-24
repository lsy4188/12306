package itlsy.entry;

import lombok.Data;

import java.util.Date;

@Data
public class UserDeletion {
    private Long id;

    private Integer idType;

    private String idCard;

    private Date createTime;

    private Date updateTime;

    private Boolean delFlag;

}