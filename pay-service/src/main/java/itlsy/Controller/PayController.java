package itlsy.Controller;

import itlsy.convert.PayRequestConvert;
import itlsy.command.PayCommand;
import itlsy.req.PayRequest;
import itlsy.resp.PayInfoRespDTO;
import itlsy.resp.PayRespDTO;
import itlsy.result.Result;
import itlsy.result.Results;
import itlsy.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 支付控制层
 */
@RestController
@RequestMapping("/api/pay-service")
public class PayController {

    @Autowired
    private PayService payService;
    /**
     * 公共支付接口
     * 对接常用支付方式，比如：支付宝、微信以及银行卡等
     */
//    @PostMapping("/pay/create")
//    public Result<PayRespDTO> payV1(@RequestBody PayCommand requestParam){
//        PayRequest payRequest = PayRequestConvert.command2PayRequest(requestParam);
//        return Results.success(payService.commonPay(payRequest));
//    }
    /**
     * 公共支付接口
     * 对接常用支付方式，比如：支付宝、微信以及银行卡等
     */
    @PostMapping("/pay/create")
    public Result<Long> payV2(@RequestBody PayCommand requestParam){
        PayRequest payRequest = PayRequestConvert.command2PayRequest(requestParam);
        Long UserId = payService.testPay(payRequest);
        return Results.success(UserId);
    }
    /**
     * 跟据订单号查询支付单详情
     */
    @GetMapping("/pay/query/order-sn")
    public Result<PayInfoRespDTO>getPayInfoByOrderSn(@RequestParam("orderSn") String orderSn){
        return Results.success(payService.getPayInfoByOrderSn(orderSn));
    }

    /**
     * 跟据支付流水号查询支付单详情
     */
    @GetMapping("/pay/query/pay-sn")
    public Result<PayInfoRespDTO> getPayInfoByPaySn(@RequestParam(value = "paySn") String paySn) {
        return Results.success(payService.getPayInfoByPaySn(paySn));
    }

}
