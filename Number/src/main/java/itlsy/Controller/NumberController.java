package itlsy.Controller;

import itlsy.req.UserLoginReq;
import itlsy.req.UserRegisterReq;
import itlsy.req.MemberSendCodeReq;
import itlsy.req.UserUpdateReq;
import itlsy.resp.*;
import itlsy.result.Result;
import itlsy.result.Results;
import itlsy.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/number")
@Slf4j
public class NumberController {
    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public Result<UserRegisterResp> register(@Valid @RequestBody UserRegisterReq req){
        return Results.success(userService.register(req));
    }

    @PostMapping("/login")
    public Result<UserLoginResp> login(@Valid @RequestBody UserLoginReq req) {
        return Results.success(userService.login(req));
    }

    @GetMapping("/logout")
    public Result<Void> logout(@Valid @RequestParam(required = false) String accessToken) {
        userService.logout(accessToken);
        return Results.success();
    }
    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/query")
    public CommonResp<UserQueryResp>queryUserByUsername(@RequestParam("username") @NotEmpty String username){
        return new CommonResp<>(userService.queryUserByUsername(username));
    }
    /**
     * 根据用户名查询用户无脱敏信息
     */
    @GetMapping("/actual/query")
    public Result<UserQueryActualRespDTO>queryActualUserByUsername(@RequestParam("username") @NotEmpty String username) {
        return Results.success(userService.queryActualUserByUsername(username));
    }

    @PostMapping("/update")
    public Result<Void> updateUser(@RequestBody @Valid UserUpdateReq requestParam){
        userService.update(requestParam);
        return Results.success();
    }

}
