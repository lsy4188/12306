//package itlsy.Controller.feign;
//
//
//import itlsy.req.NumberTicketReq;
//import itlsy.resp.CommonResp;
//import itlsy.service.TicketService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/number/feign/ticket")
//public class FeignTicketController {
//
//    @Autowired
//    private TicketService ticketService;
//
//    @PostMapping("/save")
//    public CommonResp<Object> save(@Valid @RequestBody NumberTicketReq req) throws Exception {
//        ticketService.save(req);
//        return new CommonResp<>();
//    }
//}
