package itlsy.Controller;


import itlsy.resp.StationQueryResp;
import itlsy.resp.StationQueryRespDTO;
import itlsy.resp.TrainStationQueryRespDTO;
import itlsy.result.Result;
import itlsy.result.Results;
import itlsy.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/business/station")
public class StationController {

    @Autowired
    private  StationService stationService;

    @GetMapping("/all")
    public Result<List<StationQueryRespDTO>> queryList() {
        List<StationQueryRespDTO> list = stationService.queryAll();
        return Results.success(list);
    }

    /**
     * 根据列车 ID 查询站点信息
     */
    @GetMapping("query")
    public Result<List<TrainStationQueryRespDTO>>listTrainStationQuery(String trainId){
        return Results.success(stationService.listTrainStationQuery(trainId));
    }

}
