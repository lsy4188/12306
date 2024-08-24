package itlsy.mq.produce;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息发送事件基础扩充属性实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseSendExtendDTO {
    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 交换机
     */
    private String exchange;

    /**
     * 路由键
     */
    private String routingKey;

    /**
     * 消息体
     */
    private String message;

    /**
     * 业务标识
     */
    private String keys;

    /**
     * 发送消息超时时间
     */
    private Long sentTimeout;

    /**
     * 延迟消息
     */
    private Integer delayLevel;
}
