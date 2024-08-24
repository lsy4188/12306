package itlsy.Controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateUtil;
import itlsy.command.PayCallbackCommand;
import itlsy.convert.PayCallbackRequestConvert;
import itlsy.enums.PayChannelEnum;
import itlsy.req.PayCallbackRequest;
import itlsy.strategy.AbstractStrategyChoose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付结果回调
 */
@RestController
@RequestMapping("/api/pay-service")
public class PayCallbackController {

    @Autowired
    private AbstractStrategyChoose abstractStrategyChoose;

    /**
     * 支付宝回调
     * 调用支付宝支付后，支付宝会调用此接口发送支付结果
     */
    @PostMapping("/callback/alipay")
    public void callbackAlipay(@RequestParam Map<String, Object> requestParam){
        // 将requestParam转换为PayCallbackCommand对象
        PayCallbackCommand payCallbackCommand = BeanUtil.mapToBean(requestParam, PayCallbackCommand.class, true, CopyOptions.create());
        // 设置支付渠道
        payCallbackCommand.setChannel(PayChannelEnum.ALI_PAY.getCode());
        // 设置订单请求ID
        payCallbackCommand.setOrderRequestId(requestParam.get("out_trade_no").toString());
        // 设置支付时间
        payCallbackCommand.setGmtPayment(DateUtil.parse(requestParam.get("gmt_payment").toString()));
        // 将PayCallbackCommand对象转换为PayCallbackRequest对象
        PayCallbackRequest payCallbackRequest = PayCallbackRequestConvert.command2PayCallbackRequest(payCallbackCommand);
        /**
         * {@link AliPayCallbackHandler}
         */
        // 策略模式：通过策略模式封装支付回调渠道，支付回调时动态选择对应的支付回调组件
        abstractStrategyChoose.chooseAndExecute(payCallbackRequest.buildMark(),payCallbackRequest);
    }
}
