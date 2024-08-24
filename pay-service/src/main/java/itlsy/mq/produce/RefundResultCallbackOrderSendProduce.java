package itlsy.mq.produce;

import cn.hutool.core.util.SerializeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import itlsy.mq.event.PayResultCallbackOrderEvent;
import itlsy.mq.event.RefundResultCallbackOrderEvent;
import itlsy.mq.message.MessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 退款结果回调订单生产者
 */
@Slf4j
@Component
public class RefundResultCallbackOrderSendProduce extends AbstractCommonSendProduceTemplate<RefundResultCallbackOrderEvent> {

    private final ConfigurableEnvironment environment;

    public RefundResultCallbackOrderSendProduce(@Autowired RabbitTemplate rabbitTemplate,@Autowired ConfigurableEnvironment environment) {
        super(rabbitTemplate);
        this.environment=environment;
    }

    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam(RefundResultCallbackOrderEvent messageSendEvent) {
        return BaseSendExtendDTO.builder()
                .eventName("全部退款或部分退款结果回调订单")
                .exchange(environment.getProperty("mq.refund.exchange.delay.close.order"))
                .keys(messageSendEvent.getOrderSn())
                .routingKey(environment.getProperty("mq.refund.routing.key.delay.close.order"))
                .sentTimeout(2000L)
                .delayLevel(1)
                .build();
    }

    @Override
    protected Message buildMessage(RefundResultCallbackOrderEvent messageSendEvent, BaseSendExtendDTO requestParam) {
        String keys = StrUtil.isEmpty(requestParam.getKeys()) ? UUID.randomUUID().toString() : requestParam.getKeys();
        Message message = MessageBuilder
                .withBody(SerializeUtil.serialize(JSONUtil.toJsonStr(messageSendEvent)))
                .setHeader("keys", keys)
                .build();
        return message;
    }
}
