package itlsy.handler.filter.purchase;

import itlsy.req.PurchaseTicketReqDTO;

/**
 * 购票流程过滤器之验证乘客是否重复购买
 */
public class TrainPurchaseTicketRepeatChainHandler implements TrainPurchaseTicketChainFilter<PurchaseTicketReqDTO> {
    @Override
    public void handler(PurchaseTicketReqDTO requestParam) {
        //TODO 重复购票验证
    }

    @Override
    public int getOrder() {
        return 30;
    }
}
