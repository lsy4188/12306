package itlsy.handler;

import itlsy.enums.UserRegisterErrorCodeEnum;
import itlsy.exception.ClientException;
import itlsy.req.UserRegisterReq;
import itlsy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *  用户注册用户名唯一检验
 */
@Component
@RequiredArgsConstructor
public class UserRegisterHasUsernameChainHandler implements UserRegisterCreateChainFilter<UserRegisterReq> {

    private final UserService userService;
    @Override
    public void handler(UserRegisterReq requestParam) {
        if (userService.hasUsername(requestParam.getUsername())){
            throw new ClientException(UserRegisterErrorCodeEnum.HAS_USERNAME_NOTNULL.message());
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
