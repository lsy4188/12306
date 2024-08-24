package itlsy.service;

import itlsy.dto.OrderItemStatusReversalDTO;

public interface OrderItemService {

    /**
     * 子订单状态反转
     *
     * @param requestParam 请求参数
     */
    void orderItemStatusReversal(OrderItemStatusReversalDTO requestParam);
}
