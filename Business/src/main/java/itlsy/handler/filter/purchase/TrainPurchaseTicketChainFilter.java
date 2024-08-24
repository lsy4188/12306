package itlsy.handler.filter.purchase;

import itlsy.chain.AbstractChainHandler;
import itlsy.enums.TicketChainMarkEnum;
import itlsy.req.PurchaseTicketReqDTO;

/**
 * 列车购买车票过滤器
 */
public interface TrainPurchaseTicketChainFilter<T extends PurchaseTicketReqDTO> extends AbstractChainHandler<PurchaseTicketReqDTO> {
    @Override
    default String mark() {
        return TicketChainMarkEnum.TRAIN_PURCHASE_TICKET_FILTER.name();
    }
}
