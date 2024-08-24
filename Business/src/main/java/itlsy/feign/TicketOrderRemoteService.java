package itlsy.feign;

import itlsy.feign.dto.TicketOrderCreateRemoteReqDTO;
import itlsy.feign.dto.TicketOrderDetailRespDTO;
import itlsy.feign.dto.TicketOrderPassengerDetailRespDTO;
import itlsy.feign.fallback.TicketOrderRemoteFallbackFactory;
import itlsy.req.CancelTicketOrderReqDTO;
import itlsy.req.TicketOrderItemQueryReqDTO;
import itlsy.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 车票订单远程服务调用
 */
@FeignClient(value = "order-service",fallbackFactory = TicketOrderRemoteFallbackFactory.class )
public interface TicketOrderRemoteService {
    /**
     * 创建车票订单
     *
     * @param requestParam 创建车票订单请求参数
     * @return 订单号
     */
    @PostMapping("/api/order/create")
    public Result<String> createTicketOrder(@RequestBody TicketOrderCreateRemoteReqDTO requestParam);

    /**
     * 车票订单取消
     *
     * @param requestParam 车票订单取消入参
     * @return 订单取消返回结果
     */
    @PostMapping("/api/order/cancel")
    Result<Void> cancelTicketOrder(@RequestBody CancelTicketOrderReqDTO requestParam);
    /**
     * 跟据订单号查询车票订单
     *
     * @param orderSn 列车订单号
     * @return 列车订单记录
     */
    @GetMapping("/api/order/query")
    Result<TicketOrderDetailRespDTO>  queryTicketOrderByOrderSn(@RequestParam(value = "orderSn") String orderSn);

    /**
     * 车票订单关闭
     *
     * @param requestParam 车票订单关闭入参
     * @return 关闭订单返回结果
     */
    @PostMapping("/api/order/close")
    Result<Boolean> closeTickOrder(CancelTicketOrderReqDTO requestParam);

    /**
     * 跟据子订单记录id查询车票子订单详情
     * @SpringQueryMap的作用：用于将请求参数映射到Java对象
     */
    @GetMapping("/api/order/item/query")
    Result<List<TicketOrderPassengerDetailRespDTO>> queryTicketItemOrderById(@SpringQueryMap TicketOrderItemQueryReqDTO ticketOrderItemQueryReqDTO);

}
