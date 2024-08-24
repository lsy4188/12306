package itlsy.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.google.protobuf.ServiceException;
import itlsy.enums.PayChannelEnum;
import itlsy.enums.PayTradeTypeEnum;
import itlsy.enums.TradeStatusEnum;
import itlsy.handler.base.AbstractRefundHandler;
import itlsy.properties.AliPayProperties;
import itlsy.req.AliRefundRequest;
import itlsy.req.RefundRequest;
import itlsy.resp.RefundResponse;
import itlsy.strategy.AbstractExecuteStrategy;
import itlsy.util.SnowUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 阿里支付组件
 */
@Service
@Slf4j
public class AliRefundNativeHandler extends AbstractRefundHandler implements AbstractExecuteStrategy<RefundRequest, RefundResponse> {

    @Autowired
    private AliPayProperties aliPayProperties;

    private final static String SUCCESS_CODE = "10000";

    private final static String FUND_CHANGE = "Y";
    @Override
    public String mark() {
        return StrBuilder.create()
                .append(PayChannelEnum.ALI_PAY.name())
                .append("_")
                .append(PayTradeTypeEnum.NATIVE.name())
                .append("_")
                .append(TradeStatusEnum.TRADE_CLOSED.tradeCode())
                .toString();
    }

    @Override
    public RefundResponse executeResp(RefundRequest requestParam) {
        return refund(requestParam);
    }

    @SneakyThrows(value = AlipayApiException.class)
    @Override
    public RefundResponse refund(RefundRequest payRequest) {
        AliRefundRequest aliRefundRequest = payRequest.getAliRefundRequest();
        AlipayConfig alipayConfig = BeanUtil.copyProperties(aliPayProperties, AlipayConfig.class);
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(aliRefundRequest.getOrderSn());
        model.setTradeNo(aliRefundRequest.getTradeNo());
        BigDecimal payAmount = aliRefundRequest.getPayAmount();
        BigDecimal refundAmount = payAmount.divide(new BigDecimal(100));
        model.setRefundAmount(refundAmount.toString());
        model.setOutRequestNo(SnowUtil.getSnowflakeNextIdStr());
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        try {
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            String responseJson = JSONObject.toJSONString(response);
            log.info("发起支付宝退款，订单号：{}，交易凭证号：{}，退款金额：{} \n调用退款响应：\n\n{}\n",
                    aliRefundRequest.getOrderSn(),
                    aliRefundRequest.getTradeNo(),
                    aliRefundRequest.getPayAmount(),
                    responseJson);
            if (!StrUtil.equals(SUCCESS_CODE, response.getCode()) || !StrUtil.equals(FUND_CHANGE, response.getFundChange())) {
                throw new RuntimeException("退款失败");
            }
            return new RefundResponse(TradeStatusEnum.TRADE_CLOSED.tradeCode(), response.getTradeNo());
        } catch (AlipayApiException e) {
            throw new RuntimeException("调用支付宝退款异常");
        }
    }
}
