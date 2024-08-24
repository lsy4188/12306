package itlsy.service;

import itlsy.req.MemberLoginReq;
import itlsy.req.MemberRegisterReq;
import itlsy.req.MemberSendCodeReq;
import itlsy.resp.*;

public interface Numberservice {

    CommonResp count();

    /**
     * 用户注册
     * @param req
     * @return
     */
    NumberRegisterResp register(MemberRegisterReq req);

    CommonResp forgetPassword(MemberRegisterReq req);

    CommonResp<String> sendCode(MemberSendCodeReq req);

    /**
     * 用户登录
     * @param req
     * @return
     */
    MemberLoginResp login(MemberLoginReq req);

    /**
     * 用户退出登录
     *
     * @param accessToken 用户登录 Token 凭证
     */
    void logout(String accessToken);

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    UserQueryResp queryUserByUsername(String username);
    /**
     * 根据用户名查询用户无脱敏信息
     *
     * @param username 用户名
     * @return 用户详细信息
     */
    UserQueryActualRespDTO queryActualUserByUsername(String username);
}
