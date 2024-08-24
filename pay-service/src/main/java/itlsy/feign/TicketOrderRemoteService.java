package itlsy.feign;

import itlsy.feign.dto.TicketOrderDetailRespDTO;
import itlsy.feign.fallbackfactory.TicketOrderRemoteServiceFallbackFactory;
import itlsy.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *车票订单远程服务调用
 */
@FeignClient(value = "order-service",fallbackFactory = TicketOrderRemoteServiceFallbackFactory.class)
public interface TicketOrderRemoteService {
    /**
     * 跟据订单号查询车票订单
     *
     * @param orderSn 列车订单号
     * @return 列车订单记录
     */
    @GetMapping("/api/order/query")
    Result<TicketOrderDetailRespDTO> queryTicketOrderByOrderSn(@RequestParam(value = "orderSn") String orderSn);
}
