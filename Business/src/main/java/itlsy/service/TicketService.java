package itlsy.service;

import itlsy.req.*;
import itlsy.resp.RefundTicketRespDTO;
import itlsy.resp.TicketPurchaseRespDTO;
import itlsy.resp.TicketQueryRespDTO;

public interface TicketService {

    public void save(NumberTicketReq req) throws Exception;

    /**
     *  根据条件分页查询车票
     * @param req
     * @return
     */
    public TicketQueryRespDTO queryList(TicketPageQueryReqDTO req);

    public void delete(Long id);

    /**
     *购买车票
     * @param requestParam 车票购买请求参数
     * @return 订单号
     */
    TicketPurchaseRespDTO purchaseTickets(PurchaseTicketReqDTO requestParam);
    /**
     * 取消车票订单
     *
     * @param requestParam 取消车票订单入参
     */
    void cancelTicketOrder(CancelTicketOrderReqDTO requestParam);
    /**
     * 公共退款接口
     *
     * @param requestParam 退款请求参数
     * @return 退款返回详情
     */
    RefundTicketRespDTO commonTicketRefund(RefundTicketReqDTO requestParam);
}
