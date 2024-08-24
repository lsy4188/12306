package itlsy.mq.produce;

import cn.hutool.core.util.ByteUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.SerializeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import itlsy.mq.event.PayResultCallbackOrderEvent;
import itlsy.mq.message.MessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.protocol.tri.compressor.MessageEncoding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 支付结果回调订单生产者
 */
@Slf4j
@Service
public class PayResultCallbackOrderSendProduce extends AbstractCommonSendProduceTemplate<PayResultCallbackOrderEvent> {

    private ConfigurableEnvironment environment;
    public PayResultCallbackOrderSendProduce( @Autowired RabbitTemplate rabbitTemplate,@Autowired ConfigurableEnvironment environment) {
        super(rabbitTemplate);
        this.environment=environment;
    }

    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam(PayResultCallbackOrderEvent messageSendEvent) {
        return BaseSendExtendDTO.builder()
                .eventName("支付结果回调订单")
                .exchange(environment.getProperty("mq.pay.exchange.delay.close.order"))
                .keys(messageSendEvent.getOrderSn())
                .routingKey(environment.getProperty("mq.pay.routing.key.delay.close.order"))
                .sentTimeout(10000L)
                .delayLevel(1)
                .build();
    }

    @Override
    protected Message buildMessage(PayResultCallbackOrderEvent messageSendEvent, BaseSendExtendDTO requestParam) {
        String keys = StrUtil.isEmpty(requestParam.getKeys()) ? UUID.randomUUID().toString() : requestParam.getKeys();
        Message message = MessageBuilder
                .withBody(SerializeUtil.serialize(JSONUtil.toJsonStr(messageSendEvent)))
                .setHeader("keys", keys)
                .setExpiration(requestParam.getSentTimeout() + "")
                .build();
        return message;
    }
}
