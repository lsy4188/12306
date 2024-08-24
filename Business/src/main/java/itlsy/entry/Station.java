package itlsy.entry;

import lombok.Data;

import java.util.Date;

@Data
public class Station {
    private Long id;

    private String code;

    private String name;

    private String spell;

    private String region;

    private String regionName;

    private Date createTime;

    private Date updateTime;

    private Boolean delFlag;
}