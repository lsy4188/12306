package itlsy.Controller;

import itlsy.req.MemberLoginReq;
import itlsy.req.MemberRegisterReq;
import itlsy.req.MemberSendCodeReq;
import itlsy.resp.*;
import itlsy.result.Result;
import itlsy.result.Results;
import itlsy.service.Numberservice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/number")
@Slf4j
public class NumberController {
    @Autowired
    private Numberservice numberservice;

    @GetMapping("/add")
    public CommonResp add(){
        return numberservice.count();
    }


    @PostMapping("/forget-password")
    public CommonResp forgetPassword(@Valid @RequestBody MemberRegisterReq req){
        return numberservice.forgetPassword(req);
    }

    @PostMapping("/send-code")
    public CommonResp<String> sendCode(@Valid @RequestBody MemberSendCodeReq req) {
        CommonResp<String> code = numberservice.sendCode(req);
        return code;
    }

    @PostMapping("/register")
    public CommonResp<NumberRegisterResp> register(@Valid @RequestBody MemberRegisterReq req){
        NumberRegisterResp register = numberservice.register(req);
        return new CommonResp<>(register);
    }

    @PostMapping("/login")
    public CommonResp<MemberLoginResp> login(@Valid @RequestBody MemberLoginReq req) {
        MemberLoginResp resp = numberservice.login(req);
        return new CommonResp<>(resp);
    }

    @GetMapping("/logout")
    public CommonResp logout(@Valid @RequestParam(required = false) String accessToken) {
        numberservice.logout(accessToken);
        return new CommonResp();
    }
    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/query")
    public CommonResp<UserQueryResp>queryUserByUsername(@RequestParam("username") @NotEmpty String username){
        return new CommonResp<>(numberservice.queryUserByUsername(username));
    }
    /**
     * 根据用户名查询用户无脱敏信息
     */
    @GetMapping("/actual/query")
    public Result<UserQueryActualRespDTO>queryActualUserByUsername(@RequestParam("username") @NotEmpty String username) {
        return Results.success(numberservice.queryActualUserByUsername(username));
    }

}
