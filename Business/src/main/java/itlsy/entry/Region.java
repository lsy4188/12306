package itlsy.entry;

import lombok.Data;

import java.util.Date;

@Data
public class Region {
    private Long id;

    private String name;

    private String fullName;

    private String code;

    private String initial;

    private String spell;

    private Boolean popularFlag;

    private Date createTime;

    private Date updateTime;

    private Boolean delFlag;


}