package itlsy.handler;

import cn.hutool.core.util.ObjectUtil;
import itlsy.enums.UserRegisterErrorCodeEnum;
import itlsy.exception.ClientException;
import itlsy.req.UserRegisterReq;
import org.springframework.stereotype.Component;



/**
 * 用户注册参数必填检验
 */
@Component
public final class UserRegisterParamNotNullChainHandler implements UserRegisterCreateChainFilter<UserRegisterReq> {

    @Override
    public void handler(UserRegisterReq requestParam) {
        if (ObjectUtil.isNull(requestParam.getUsername())){
            throw new ClientException(UserRegisterErrorCodeEnum.USER_NAME_NOTNULL);
        }else if (ObjectUtil.isNull(requestParam.getPassword())) {
            throw new ClientException(UserRegisterErrorCodeEnum.PASSWORD_NOTNULL);
        } else if (ObjectUtil.isNull(requestParam.getPhone())) {
            throw new ClientException(UserRegisterErrorCodeEnum.PHONE_NOTNULL);
        } else if (ObjectUtil.isNull(requestParam.getIdType())) {
            throw new ClientException(UserRegisterErrorCodeEnum.ID_TYPE_NOTNULL);
        } else if (ObjectUtil.isNull(requestParam.getIdCard())) {
            throw new ClientException(UserRegisterErrorCodeEnum.ID_CARD_NOTNULL);
        } else if (ObjectUtil.isNull(requestParam.getMail())) {
            throw new ClientException(UserRegisterErrorCodeEnum.MAIL_NOTNULL);
        } else if (ObjectUtil.isNull(requestParam.getRealName())) {
            throw new ClientException(UserRegisterErrorCodeEnum.REAL_NAME_NOTNULL);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
