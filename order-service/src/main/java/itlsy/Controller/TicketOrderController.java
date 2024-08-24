package itlsy.Controller;

import cn.crane4j.annotation.AutoOperate;
import itlsy.req.*;
import itlsy.resp.TicketOrderDetailRespDTO;
import itlsy.resp.TicketOrderDetailSelfRespDTO;
import itlsy.resp.TicketOrderPassengerDetailRespDTO;
import itlsy.result.Result;
import itlsy.result.Results;
import itlsy.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")

public class TicketOrderController {

    @Autowired
    private OrderService orderService;
    /**
     * 根据订单号查询车票订单
     */
    @GetMapping("/query")
    public Result<TicketOrderDetailRespDTO>queryTicketOrderByOrderSn(@RequestParam("orderSn") String orderSn){
        return Results.success(orderService.queryTicketOrderByOrderSn(orderSn));
    }

    @GetMapping("/item/query")
    public Result<List<TicketOrderPassengerDetailRespDTO>>queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam){
        return Results.success(orderService.queryTicketItemOrderById(requestParam));
    }


    /**
     * 分页查询车票订单
     */
    @AutoOperate(type = TicketOrderDetailRespDTO.class, on = "data.records") //声明自动填充
    @GetMapping("/page")
    public Result<PageResponse<TicketOrderDetailRespDTO>> pageTicketOrder(TicketOrderPageQueryReqDTO req){
        return Results.success(orderService.pageTicketOrder(req));
    }

    /**
     * 分页查询本人车票订单
     */
    @GetMapping("/self/page")
    public Result<PageResponse<TicketOrderDetailSelfRespDTO>> pageSelfTicketOrder(TicketOrderSelfPageQueryReqDTO requestParam) {
        return Results.success(orderService.pageSelfTicketOrder(requestParam));
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<String>createTicketOrder(@RequestBody TicketOrderCreateReqDTO requestParam){
        return Results.success(orderService.createTicketOrder(requestParam));
    }
    /**
     * 车票订单取消
     */
    @PostMapping("/cancel")
    public Result<Boolean>cancelTicketOrder(@RequestBody CancelTicketOrderReqDTO requestParam){
        return Results.success(orderService.cancelTicketOrder(requestParam));
    }
    /**
     * 车票订单关闭
     */
    @PostMapping("/close")
    public Result<Boolean>closeTickOrder(@RequestBody CancelTicketOrderReqDTO requestParam){
        return Results.success(orderService.closeTickOrder(requestParam));
    }

}
