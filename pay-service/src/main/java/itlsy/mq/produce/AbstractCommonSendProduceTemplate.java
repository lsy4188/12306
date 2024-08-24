package itlsy.mq.produce;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import itlsy.mq.message.MessageWrapper;
import itlsy.util.SnowUtil;
import jakarta.websocket.SendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * RabbitMQ 抽象公共发送消息组件
 */
// TODO 待修改
@Slf4j
@RequiredArgsConstructor //采用构造函数的方法注入
public abstract class AbstractCommonSendProduceTemplate<T> {

    private final RabbitTemplate rabbitTemplate;
    /**
     * 构建消息发送事件基础扩充属性实体
     *
     * @param messageSendEvent 消息发送事件
     * @return 扩充属性实体
     */
    protected abstract BaseSendExtendDTO buildBaseSendExtendParam(T messageSendEvent);

    /**
     * 构建消息基本参数，请求头、Keys...
     *
     * @param messageSendEvent 消息发送事件
     * @param requestParam     扩充属性实体
     * @return 消息基本参数
     */
    protected abstract Message buildMessage(T messageSendEvent, BaseSendExtendDTO requestParam);

    /**
     * 消息事件通用发送
     *
     * @param messageSendEvent 消息发送事件
     * @return 消息发送返回结果
     */
    public void sendMessage(T messageSendEvent) {
        BaseSendExtendDTO baseSendExtendDTO = buildBaseSendExtendParam(messageSendEvent);
        StringBuilder exchange = StrUtil.builder().append(baseSendExtendDTO.getExchange());
        StringBuilder routingKey = StrUtil.builder().append(baseSendExtendDTO.getRoutingKey());
        CorrelationData correlationData = new CorrelationData(SnowUtil.getSnowflakeNextId()+"");
        correlationData.getFuture().thenAccept(confirm -> {
            if (confirm.isAck()){
                log.debug("[{}] 消息发送成功，消息体：{}", baseSendExtendDTO.getEventName(), JSON.toJSONString(messageSendEvent));
            }
            else {
                log.error("[{}] 消息发送失败,原因:{}，消息体：{}", baseSendExtendDTO.getEventName(),confirm.getReason(), JSON.toJSONString(messageSendEvent));
            }
        });

        try {
            rabbitTemplate.convertAndSend(
                    exchange.toString(),
                    routingKey.toString(),
                    buildMessage(messageSendEvent, baseSendExtendDTO),
                    correlationData
            );
        } catch (AmqpException e) {
            log.error("[{}] 消息发送失败，消息体：{}", baseSendExtendDTO.getEventName(), JSON.toJSONString(messageSendEvent), e);
            throw new RuntimeException(e);
        }
    }
}
