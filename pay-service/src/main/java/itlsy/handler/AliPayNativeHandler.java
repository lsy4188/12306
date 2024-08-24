package itlsy.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.google.protobuf.ServiceException;
import itlsy.enums.PayChannelEnum;
import itlsy.enums.PayTradeTypeEnum;
import itlsy.handler.base.AbstractPayHandler;
import itlsy.properties.AliPayProperties;
import itlsy.req.AliPayRequest;
import itlsy.req.PayRequest;
import itlsy.resp.PayResponse;
import itlsy.strategy.AbstractExecuteStrategy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 阿里支付组件
 */
@Slf4j
@Service
public class AliPayNativeHandler extends AbstractPayHandler implements AbstractExecuteStrategy<PayRequest, PayResponse> {

    @Autowired
    private AliPayProperties aliPayProperties;

    @Override
    public String mark() {
        return StrBuilder.create()
                .append(PayChannelEnum.ALI_PAY.name())
                .append("_")
                .append(PayTradeTypeEnum.NATIVE.name())
                .toString();
    }

    @Override
    public PayResponse executeResp(PayRequest requestParam) {
        return pay(requestParam);
    }

    /**
     *@Retryable(value = ServiceException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 1.5))
     * 重试机制注解
     */
    @Override
    @SneakyThrows(value = AlipayApiException.class) //用于简化异常处理,只针对AlipayApiException
    public PayResponse pay(PayRequest payRequest) {
        AliPayRequest aliPayRequest = payRequest.getAliPayRequest();
        AlipayConfig alipayConfig = BeanUtil.copyProperties(aliPayProperties, AlipayConfig.class);
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(aliPayRequest.getOrderSn());
        model.setTotalAmount(aliPayRequest.getTotalAmount().toString());
        model.setSubject(aliPayRequest.getSubject());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(aliPayProperties.getNotifyUrl());
        request.setBizModel(model);
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            log.info("发起支付宝支付，订单号：{}，子订单号：{}，订单请求号：{}，订单金额：{} \n调用支付返回：\n\n{}\n",
                    aliPayRequest.getOrderSn(),
                    aliPayRequest.getOutOrderSn(),
                    aliPayRequest.getOrderRequestId(),
                    aliPayRequest.getTotalAmount(),
                    JSONObject.toJSONString(response));
            if (!response.isSuccess()) {
                throw new RuntimeException("调用支付宝发起支付异常");
            }
            return new PayResponse(StrUtil.replace(StrUtil.replace(response.getBody(), "\"", "'"), "\n", ""));
        } catch (AlipayApiException ex) {
            throw new RuntimeException("调用支付宝支付异常");
        }
    }
}
