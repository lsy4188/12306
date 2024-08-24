package itlsy.service;

import itlsy.req.RefundReqDTO;
import itlsy.resp.RefundRespDTO;

/**
 * 退款接口层
 */
public interface RefundService {

    /**
     * 公共退款接口(接入支付宝接口)
     *
     * @param requestParam 退款请求参数
     * @return 退款返回详情
     */
    RefundRespDTO commonRefund(RefundReqDTO requestParam);

    /**
     * 公共退款接口
     *
     * @param requestParam 退款请求参数
     * @return 退款返回详情
     */
    RefundRespDTO commonRefundV2(RefundReqDTO requestParam);
}
