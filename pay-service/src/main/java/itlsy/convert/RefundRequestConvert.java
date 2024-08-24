package itlsy.convert;

import cn.hutool.core.bean.BeanUtil;
import itlsy.enums.PayChannelEnum;
import itlsy.req.AliRefundRequest;
import itlsy.req.RefundCommand;
import itlsy.req.RefundRequest;

import java.util.Objects;

/**
 * 退款请求入参转换器
 */
public final class RefundRequestConvert {
    /**
     * {@link RefundCommand} to {@link RefundRequest}
     *
     * @param refundCommand 退款请求参数
     * @return {@link RefundRequest}
     */
    public static RefundRequest command2RefundRequest(RefundCommand refundCommand) {
        RefundRequest refundRequest = null;
        if (Objects.equals(refundCommand.getChannel(), PayChannelEnum.ALI_PAY.getCode())) {
            refundRequest = BeanUtil.copyProperties(refundCommand, AliRefundRequest.class);
        }
        return refundRequest;
    }
}
