package itlsy.convert;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import itlsy.command.PayCallbackCommand;
import itlsy.enums.PayChannelEnum;
import itlsy.req.AliPayCallbackRequest;
import itlsy.req.PayCallbackRequest;

/**
 * 支付回调请求入参转换器
 */
public final class PayCallbackRequestConvert {

    public static PayCallbackRequest command2PayCallbackRequest(PayCallbackCommand payCallbackCommand){
        PayCallbackRequest payCallbackRequest=null;
        if (ObjectUtil.equals(payCallbackCommand.getChannel(), PayChannelEnum.ALI_PAY)){
            payCallbackRequest= BeanUtil.copyProperties(payCallbackCommand, AliPayCallbackRequest.class);
            ((AliPayCallbackRequest) payCallbackRequest).setOrderRequestId(payCallbackCommand.getOrderRequestId());
        }
        return payCallbackRequest;
    }
}
