package itlsy.mq.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.SerializeUtil;
import cn.hutool.json.JSONUtil;
import itlsy.dto.OrderStatusReversalDTO;
import itlsy.enums.OrderItemStatusEnum;
import itlsy.enums.OrderStatusEnum;
import itlsy.mq.message.MessageWrapper;
import itlsy.mq.event.PayResultCallbackOrderEvent;
import itlsy.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.FutureUtils;

import java.io.Serializable;

/**
 * 支付结果回调订单消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayResultCallbackOrderConsumer  {

    private final OrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${mq.pay-result-callback-order-queue}", durable = "true"),
            exchange = @Exchange(value = "${mq.pay-result-callback-order-exchange}", ignoreDeclarationExceptions = "true"),
            key = "${mq.pay-result-callback-order-routing-key}"
    ))
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Message message){
        String jsonData = SerializeUtil.deserialize(message.getBody()).toString();
        PayResultCallbackOrderEvent payResultCallbackOrderEvent = JSONUtil.toBean(jsonData, PayResultCallbackOrderEvent.class);
        OrderStatusReversalDTO orderStatusReversalDTO = OrderStatusReversalDTO.builder()
                .orderSn(payResultCallbackOrderEvent.getOrderSn())
                .orderStatus(OrderStatusEnum.ALREADY_PAID.getStatus())
                .orderItemStatus(OrderItemStatusEnum.ALREADY_PAID.getStatus())
                .build();
        orderService.statusReversal(orderStatusReversalDTO);
        orderService.payCallbackOrder(payResultCallbackOrderEvent);
    }
}
