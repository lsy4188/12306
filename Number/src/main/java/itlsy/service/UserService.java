package itlsy.service;

import itlsy.req.UserLoginReq;
import itlsy.req.UserRegisterReq;
import itlsy.req.MemberSendCodeReq;
import itlsy.req.UserUpdateReq;
import itlsy.resp.*;

public interface UserService {
    /**
     * 用户注册
     * @param req
     * @return
     */
    UserRegisterResp register(UserRegisterReq req);

    /**
     * 用户登录
     * @param req
     * @return
     */
    UserLoginResp login(UserLoginReq req);

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
    /**
     * 用户名是否存在
     *
     * @param username 用户名
     * @return 用户名是否存在返回结果
     */
    Boolean hasUsername(String username);
    /**
     * 根据证件类型和证件号查询注销次数
     *
     * @param idType 证件类型
     * @param idCard 证件号
     * @return 注销次数
     */
    Integer queryUserDeletionNum(Integer idType, String idCard);

    /**
     * 根据用户 ID 修改用户信息
     *
     * @param requestParam 用户信息入参
     */
    void update(UserUpdateReq requestParam);
}
