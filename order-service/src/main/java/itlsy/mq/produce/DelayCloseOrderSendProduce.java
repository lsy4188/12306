package itlsy.mq.produce;

import itlsy.mq.event.DelayCloseOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

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
                .exchange()
                .routingKey()
                .sentTimeout()
                .delayLevel()
                .build();
    }

    @Override
    protected Message buildMessage(DelayCloseOrderEvent messageSendEvent, BaseSendExtendDTO requestParam) {
        return null;
    }
}
