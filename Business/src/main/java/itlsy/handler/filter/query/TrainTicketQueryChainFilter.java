package itlsy.handler.filter.query;

import itlsy.chain.AbstractChainHandler;
import itlsy.req.TicketPageQueryReqDTO;
import itlsy.enums.TicketChainMarkEnum;

/**
 * 列车车票查询过滤器
 */
public interface TrainTicketQueryChainFilter<T extends TicketPageQueryReqDTO> extends AbstractChainHandler<TicketPageQueryReqDTO> {
    @Override
    default String mark() {
        return TicketChainMarkEnum.TRAIN_QUERY_FILTER.name();
    }
}
