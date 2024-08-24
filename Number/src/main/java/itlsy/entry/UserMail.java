package itlsy.entry;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
@Data
@Builder
public class UserMail {
    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 注销时间戳
     */
    private Long deletionTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Boolean delFlag;


}