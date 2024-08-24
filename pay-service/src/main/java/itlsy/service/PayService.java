package itlsy.service;

import itlsy.dto.PayCallbackReqDTO;
import itlsy.req.PayRequest;
import itlsy.resp.PayInfoRespDTO;
import itlsy.resp.PayRespDTO;

public interface PayService {
    /**
     * 创建支付单
     *
     * @param requestParam 创建支付单实体
     * @return 支付返回详情
     */
    PayRespDTO commonPay(PayRequest requestParam);

    /**
     * 创建支付单(测试版并未对接任何支付接口)
     *
     * @param requestParam 创建支付单实体
     */
    Long testPay(PayRequest requestParam);

    /**
     * 跟据订单号查询支付单详情
     *
     * @param orderSn 订单号
     * @return 支付单详情
     */
    PayInfoRespDTO getPayInfoByOrderSn(String orderSn);

    /**
     * 跟据支付流水号查询支付单详情
     *
     * @param paySn 支付单流水号
     * @return 支付单详情
     */
    PayInfoRespDTO getPayInfoByPaySn(String paySn);
    /**
     * 支付单回调
     *
     * @param requestParam 回调支付单实体
     */
    void callbackPay(PayCallbackReqDTO requestParam);
}
