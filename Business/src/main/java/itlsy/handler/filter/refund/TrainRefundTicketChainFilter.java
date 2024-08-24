package itlsy.handler.filter.refund;

import itlsy.chain.AbstractChainHandler;
import itlsy.enums.TicketChainMarkEnum;
import itlsy.req.RefundTicketReqDTO;

/**
 * 列车车票退款过滤器
 */
public interface TrainRefundTicketChainFilter<T extends RefundTicketReqDTO> extends AbstractChainHandler<RefundTicketReqDTO> {
    @Override
    default String mark() {
        return TicketChainMarkEnum.TRAIN_REFUND_TICKET_FILTER.name();
    }
}
