package itlsy.Controller;


import itlsy.UserContext;
import itlsy.req.PassengersRemoveReq;
import itlsy.req.PassengersSaveReq;
import itlsy.resp.CommonResp;

import itlsy.resp.PassengerActualRespDTO;
import itlsy.resp.PassengersQueryResp;
import itlsy.result.Result;
import itlsy.result.Results;
import itlsy.service.PassengerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/number/passenger")
@Slf4j
public class PassengerController {
    @Autowired
    private PassengerService passengerService;


    @PostMapping("/SaveAndUpdate")
    public Result<Void> save(@Valid @RequestBody PassengersSaveReq req){
         passengerService.SaveAndUpdate(req);
        return Results.success();
    }

    @GetMapping("/query")
    public Result<List<PassengersQueryResp>> queryPassengersList() {
        List<PassengersQueryResp> passengersList = passengerService.listPassengerQueryByUsername(UserContext.getUserName());
        return Results.success(passengersList);
    }

    @GetMapping("/query/ids")
    public Result<List<PassengerActualRespDTO>>listPassengerQueryByIds(@RequestParam("username") String username, @RequestParam("ids") List<Long> ids){
        return Results.success(passengerService.listPassengerQueryByIds(username,ids));
    }

//    @GetMapping("/query-list")
//    public CommonResp<PageResp<PassengersQueryResp>> queryList(@Valid PassengersQueryReq req){
//        req.setNumberId(LoginMemberContext.getId());
//        PageResp<PassengersQueryResp> list = passengerService.queryList(req);
//        return new CommonResp<>(list);
//    }

    @PostMapping("/remove")
    public Result<Void> delete(@RequestBody PassengersRemoveReq req){
        passengerService.removePassenger(req);
        return Results.success();
    }
}
