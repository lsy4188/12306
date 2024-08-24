package itlsy.entry;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 用户名复用表实体
 */
@Data
@Builder
public class UserReuse {
    private Long id;

    private String username;

    private Date createTime;

    private Date updateTime;

    private Boolean delFlag;
}