package itlsy.mq.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.SerializeUtil;
import cn.hutool.json.JSONUtil;
import itlsy.dto.OrderItemStatusReversalDTO;
import itlsy.entry.OrderItem;
import itlsy.enums.OrderItemStatusEnum;
import itlsy.enums.OrderStatusEnum;
import itlsy.mq.event.PayResultCallbackOrderEvent;
import itlsy.mq.message.MessageWrapper;
import itlsy.mq.event.RefundResultCallbackOrderEvent;
import itlsy.resp.TicketOrderPassengerDetailRespDTO;
import itlsy.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 退款结果回调订单消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundResultCallbackOrderConsumer {

    private final OrderItemService orderItemService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${mq.refund-result-callback-order-queue}", durable = "true"),
            exchange = @Exchange(value = "${mq.refund-result-callback-order-exchange}", ignoreDeclarationExceptions = "true"),
            key = "${mq.refund-result-callback-order-routing-key}"
    ))
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Message message){
        String jsonData = SerializeUtil.deserialize(message.getBody()).toString();
        RefundResultCallbackOrderEvent refundResultCallbackOrderEvent = JSONUtil.toBean(jsonData, RefundResultCallbackOrderEvent.class);
        Integer status = refundResultCallbackOrderEvent.getRefundTypeEnum().getCode();
        String orderSn = refundResultCallbackOrderEvent.getOrderSn();
        ArrayList<OrderItem> orderItemList = new ArrayList<>();
        List<TicketOrderPassengerDetailRespDTO> partialRefundTicketDetailList = refundResultCallbackOrderEvent.getPartialRefundTicketDetailList();
        partialRefundTicketDetailList.forEach(item->{
            OrderItem orderItem = new OrderItem();
            BeanUtil.copyProperties(item,orderItem);
            orderItemList.add(orderItem);
        });
        if (status.equals(OrderStatusEnum.PENDING_PAYMENT.getStatus())){
            OrderItemStatusReversalDTO partialRefundOrderItemStatusReversalDTO = OrderItemStatusReversalDTO.builder()
                    .orderSn(orderSn)
                    .orderStatus(OrderStatusEnum.PARTIAL_REFUND.getStatus())
                    .orderItemDOList(orderItemList)
                    .build();
            orderItemService.orderItemStatusReversal(partialRefundOrderItemStatusReversalDTO);
        } else if (status.equals(OrderStatusEnum.FULL_REFUND.getStatus())) {
            OrderItemStatusReversalDTO fullRefundOrderItemStatusReversalDTO = OrderItemStatusReversalDTO.builder()
                    .orderSn(orderSn)
                    .orderStatus(OrderStatusEnum.FULL_REFUND.getStatus())
                    .orderItemStatus(OrderItemStatusEnum.REFUNDED.getStatus())
                    .orderItemDOList(orderItemList)
                    .build();
            orderItemService.orderItemStatusReversal(fullRefundOrderItemStatusReversalDTO);
        }
    }
}
