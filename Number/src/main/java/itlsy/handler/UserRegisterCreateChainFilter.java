package itlsy.handler;

import itlsy.chain.AbstractChainHandler;
import itlsy.enums.UserChainMarkEnum;
import itlsy.req.UserRegisterReq;

/**
 * 用户注册责任链过滤器
 */
public interface UserRegisterCreateChainFilter<T extends UserRegisterReq> extends AbstractChainHandler<UserRegisterReq> {
    @Override
    default String mark(){
       return UserChainMarkEnum.USER_REGISTER_FILTER.name();
    }
}
