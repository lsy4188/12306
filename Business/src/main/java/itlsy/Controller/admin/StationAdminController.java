package itlsy.Controller.admin;


import itlsy.req.StationQueryReq;
import itlsy.req.StationSaveReq;
import itlsy.resp.CommonResp;
import itlsy.resp.PageResp;
import itlsy.resp.StationQueryResp;
import itlsy.resp.StationQueryRespDTO;
import itlsy.result.Result;
import itlsy.result.Results;
import itlsy.service.impl.StationServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/business/admin/station")
public class StationAdminController {

    @Autowired
    private StationServiceImpl stationService;


    @GetMapping("/query-list")
    public CommonResp<PageResp<StationQueryResp>> queryList(@Valid StationQueryReq req) {
        PageResp<StationQueryResp> list = stationService.queryList(req);
        return new CommonResp<>(list);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        stationService.delete(id);
        return new CommonResp<>();
    }

    @GetMapping("/query-all")
    public Result<List<StationQueryRespDTO>> queryList() {
        List<StationQueryRespDTO> list = stationService.queryAll();
        return Results.success(list);
    }

}
