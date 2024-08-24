package itlsy.Controller;

import itlsy.req.CancelTicketOrderReqDTO;
import itlsy.req.PurchaseTicketReqDTO;
import itlsy.req.RefundTicketReqDTO;
import itlsy.req.TicketPageQueryReqDTO;
import itlsy.resp.RefundTicketRespDTO;
import itlsy.resp.TicketPurchaseRespDTO;
import itlsy.resp.TicketQueryRespDTO;
import itlsy.result.Result;
import itlsy.result.Results;
import itlsy.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    /**
     * 查询用户购票信息
     * @param req
     * @return
     */
    @GetMapping("/query")
    public Result<TicketQueryRespDTO> pageListTicketQuery(@Valid TicketPageQueryReqDTO req){
        TicketQueryRespDTO ticketQueryResp = ticketService.queryList(req);
        return Results.success(ticketQueryResp);
    }

//    /**
//     * 分页查询车票订单
//     */
//    @AutoOperate(type = TicketOrderDetailRespDTO.class, on = "data.records") //声明自动填充
//    @GetMapping("/page")
//    public Result<PageResponse<TicketOrderDetailRespDTO>> pageTicketOrder(TicketOrderPageQueryReqDTO req){
//        return Results.success(orderService.pageTicketOrder(req));
//    }

    /**
     * 购票
     */
    @PostMapping("/purchase")
    public Result<TicketPurchaseRespDTO>purchaseTickets(@RequestBody PurchaseTicketReqDTO req){
        return Results.success(ticketService.purchaseTickets(req));
    }

    /**
     *取消订单
     */
    @PostMapping("/cancel")
    public Result<Void> cancelTicketOrder(@RequestBody CancelTicketOrderReqDTO requestParam){
        ticketService.cancelTicketOrder(requestParam);
        return Results.success();
    }

    /**
     * 公共退款接口
     */
    @PostMapping("/refund")
    public Result<RefundTicketRespDTO>commonTicketRefund(@RequestBody RefundTicketReqDTO requestParam){
        return Results.success(ticketService.commonTicketRefund(requestParam));
    }
}
