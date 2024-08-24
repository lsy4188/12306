package itlsy.convert;

import cn.hutool.core.bean.BeanUtil;
import itlsy.enums.PayChannelEnum;
import itlsy.req.AliPayRequest;
import itlsy.command.PayCommand;
import itlsy.req.PayRequest;

import java.util.Objects;

/**
 * 支付请求入参转换器
 */
public final class PayRequestConvert {
    /**
     * {@link PayCommand} to {@link PayRequest}
     *
     * @param payCommand 支付请求参数
     * @return {@link PayRequest}
     */
    public static PayRequest command2PayRequest(PayCommand payCommand) {
        PayRequest payRequest = null;
        if (Objects.equals(payCommand.getChannel(), PayChannelEnum.ALI_PAY.getCode())) {
            payRequest = BeanUtil.copyProperties(payCommand, AliPayRequest.class);
        }
        return payRequest;
    }
}
