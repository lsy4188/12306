
package itlsy.mq.consumer;

import cn.hutool.core.util.SerializeUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import itlsy.dto.TrainPurchaseTicketRespDTO;
import itlsy.feign.TicketOrderRemoteService;
import itlsy.mq.event.DelayCloseOrderEvent;
import itlsy.req.CancelTicketOrderReqDTO;
import itlsy.result.Result;
import itlsy.service.SeatService;
import itlsy.service.TrainSeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


import java.util.List;

/**
 * 延迟关闭订单消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DelayCloseOrderConsumer {
    private final TicketOrderRemoteService ticketOrderRemoteService;
    private final SeatService seatService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${mq.order-delay.queue}",durable = "true"),
            exchange = @Exchange(value = "${mq.order-delay.exchange}",ignoreDeclarationExceptions = "true",delayed = "true"),
            key = "${mq.order-delay.routing-key}"
    ))
    public void onMessage(Message message){
        String jsonData = SerializeUtil.deserialize(message.getBody()).toString();
        log.info("[延迟关闭订单] 开始消费：{}", JSON.toJSONString(jsonData));
        DelayCloseOrderEvent delayCloseOrderEvent = JSONUtil.toBean(jsonData, DelayCloseOrderEvent.class);
        String orderSn = delayCloseOrderEvent.getOrderSn();
        Result<Boolean>closedTickOrder;
        try {
            closedTickOrder = ticketOrderRemoteService.closeTickOrder(new CancelTicketOrderReqDTO(orderSn));
        } catch (Exception e) {
            log.error("[延迟关闭订单] 订单号：{} 远程调用订单服务失败", orderSn, e);
            throw e;
        }
        //TODO 待修改
        if (closedTickOrder.isSuccess()){
            if (!closedTickOrder.getData()) {
                log.info("[延迟关闭订单] 订单号：{} 用户已支付订单", orderSn);
                return;
            }
            String trainId = delayCloseOrderEvent.getTrainId();
            String departure = delayCloseOrderEvent.getDeparture();
            String arrival = delayCloseOrderEvent.getArrival();
            List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults = delayCloseOrderEvent.getTrainPurchaseTicketResults();
            try {
                seatService.unlock(trainId, departure, arrival, trainPurchaseTicketResults);
            } catch (Exception e) {
                log.error("[延迟关闭订单] 订单号：{} 回滚列车DB座位状态失败", orderSn, e);
                throw e;
            }
        }
    }
}
