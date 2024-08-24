//package itlsy.Controller.admin;
//
//
//import itlsy.req.TicketQueryReq;
//import itlsy.resp.CommonResp;
//import itlsy.resp.PageResp;
//import itlsy.resp.TicketQueryResp;
//import itlsy.service.Impl.TicketServiceImpl;
//import itlsy.service.TicketService;
//import jakarta.annotation.Resource;
//import jakarta.validation.Valid;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/number/admin/ticket")
//public class TicketAdminController {
//
//    @Resource
//    private TicketService ticketService;
//
//    @GetMapping("/query-list")
//    public CommonResp<PageResp<TicketQueryResp>> queryList(@Valid TicketQueryReq req) {
//        PageResp<TicketQueryResp> list = ticketService.queryList(req);
//        return new CommonResp<>(list);
//    }
//
//}