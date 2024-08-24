package itlsy.mq.produce;

import cn.hutool.core.util.SerializeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import itlsy.mq.event.DelayCloseOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 延迟关闭订单生产者
 * TODO 待修改
 */
@Slf4j
@Component
public class DelayCloseOrderSendProduce extends AbstractCommonSendProduceTemplate<DelayCloseOrderEvent> {

    private final ConfigurableEnvironment environment;

    public DelayCloseOrderSendProduce(@Autowired RabbitTemplate rabbitTemplate,@Autowired ConfigurableEnvironment environment) {
        super(rabbitTemplate);
        this.environment=environment;
    }

    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam(DelayCloseOrderEvent messageSendEvent) {
        return BaseSendExtendDTO.builder()
                .eventName("延迟关闭订单")
                .keys(messageSendEvent.getOrderSn())
                .exchange(environment.getProperty("mq.order-delay-exchange"))
                .routingKey(environment.getProperty("mq.order-delay-routing-key"))
                .sentTimeout(10000L)
                .delayLevel(1)
                .build();
    }

    @Override
    protected Message buildMessage(DelayCloseOrderEvent messageSendEvent, BaseSendExtendDTO requestParam) {
        String keys = StrUtil.isEmpty(requestParam.getKeys()) ? UUID.randomUUID().toString() : requestParam.getKeys();
        Message message = MessageBuilder
                .withBody(SerializeUtil.serialize(JSONUtil.toJsonStr(messageSendEvent)))
                .setHeader("keys", keys)
                .build();
        //添加延迟消息属性
        message.getMessageProperties().setDelay(10000);
        return message;
    }
}
