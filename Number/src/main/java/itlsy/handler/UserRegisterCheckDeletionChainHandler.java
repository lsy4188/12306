package itlsy.handler;

import itlsy.exception.ClientException;
import itlsy.req.UserRegisterReq;
import itlsy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class UserRegisterCheckDeletionChainHandler implements UserRegisterCreateChainFilter<UserRegisterReq>{

    private final UserService userService;
    @Override
    public void handler(UserRegisterReq requestParam) {
        Integer userDeletionNum =userService.queryUserDeletionNum(requestParam.getIdType(), requestParam.getIdCard());
        if (userDeletionNum>3){
            throw new ClientException("证件号多次注销账号已被加入黑名单");
        }
    }
    @Override
    public int getOrder() {
        return 2;
    }
}
