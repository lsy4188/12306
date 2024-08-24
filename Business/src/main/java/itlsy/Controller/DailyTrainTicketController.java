package itlsy.Controller;

import itlsy.req.DailyTrainTicketQueryReq;
import itlsy.resp.CommonResp;
import itlsy.resp.DailyTrainTicketQueryResp;
import itlsy.resp.PageResp;
import itlsy.service.DailyTrainTicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/daily-train-ticket")
public class DailyTrainTicketController {

    @Autowired
    private DailyTrainTicketService dailyTrainTicketService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<DailyTrainTicketQueryResp>> queryList(@Valid DailyTrainTicketQueryReq req) {
        PageResp<DailyTrainTicketQueryResp> list = dailyTrainTicketService.queryList(req);
        return new CommonResp<>(list);
    }



}
