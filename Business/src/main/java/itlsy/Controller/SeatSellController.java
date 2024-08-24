package itlsy.Controller;

import itlsy.req.SeatSellReq;
import itlsy.resp.CommonResp;
import itlsy.resp.SeatSellResp;
import itlsy.service.DailyTrainSeatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/business/seat-sell")
public class SeatSellController {

    @Autowired
    private DailyTrainSeatService dailyTrainSeatService;

    @GetMapping("/query")
    public CommonResp<List<SeatSellResp>> query(@Valid SeatSellReq req) {
        List<SeatSellResp> query = dailyTrainSeatService.query(req);
        return new CommonResp<>(query);
    }
}
