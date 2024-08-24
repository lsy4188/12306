package itlsy.Controller;

import itlsy.req.DailyTrainStationQueryAllReq;
import itlsy.resp.CommonResp;
import itlsy.resp.DailyTrainStationQueryResp;
import itlsy.service.DailyTrainStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/business/daily-train-station")
public class DailyTrainStationController {

    @Autowired
    private DailyTrainStationService dailyTrainStationService;

    @GetMapping("/query-by-train-code")
    public CommonResp<List<DailyTrainStationQueryResp>>queryByTrain(DailyTrainStationQueryAllReq req){
        List<DailyTrainStationQueryResp> dailyTrainStations = dailyTrainStationService.queryByTrain(req);
        return new CommonResp<>(dailyTrainStations);
    }
}
