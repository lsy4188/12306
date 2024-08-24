package itlsy.feign.fallback;

import itlsy.feign.TicketOrderRemoteService;
import itlsy.feign.dto.TicketOrderCreateRemoteReqDTO;
import itlsy.feign.dto.TicketOrderDetailRespDTO;
import itlsy.feign.dto.TicketOrderPassengerDetailRespDTO;
import itlsy.req.CancelTicketOrderReqDTO;
import itlsy.req.TicketOrderItemQueryReqDTO;
import itlsy.result.Result;
import itlsy.result.Results;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TicketOrderRemoteFallbackFactory implements FallbackFactory<TicketOrderRemoteService> {

    @Override
    public TicketOrderRemoteService create(Throwable cause) {
        return new TicketOrderRemoteService() {
            @Override
            public Result<String> createTicketOrder(TicketOrderCreateRemoteReqDTO requestParam) {
                return Results.success("createTicketOrder-->fallback");
            }

            @Override
            public Result<Void> cancelTicketOrder(CancelTicketOrderReqDTO requestParam) {
                return Results.success();
            }

            @Override
            public Result<TicketOrderDetailRespDTO> queryTicketOrderByOrderSn(String orderSn) {
                return Results.success(null);
            }

            @Override
            public Result<Boolean> closeTickOrder(CancelTicketOrderReqDTO requestParam) {
                return Results.success(true);
            }

            @Override
            public Result<List<TicketOrderPassengerDetailRespDTO>> queryTicketItemOrderById(TicketOrderItemQueryReqDTO ticketOrderItemQueryReqDTO) {
                return Results.success(null);
            }
        };
    }
}
