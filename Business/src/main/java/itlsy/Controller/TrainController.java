package itlsy.Controller;

import itlsy.resp.CommonResp;
import itlsy.resp.TrainQueryResp;
import itlsy.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/business/train")
public class TrainController {

    @Autowired
    private TrainService trainService;

    @GetMapping("/all")
    public CommonResp<List<TrainQueryResp>> queryList() {
        List<TrainQueryResp> list = trainService.queryAll();
        return new CommonResp<>(list);
    }

}
