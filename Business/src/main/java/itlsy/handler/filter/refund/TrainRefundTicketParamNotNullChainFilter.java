package itlsy.handler.filter.refund;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import itlsy.enums.RefundTypeEnum;
import itlsy.req.RefundTicketReqDTO;
import org.springframework.stereotype.Component;

/**
 * 列车车票退款流程过滤器之验证数据是否为空或空的字符串
 */
@Component
public class TrainRefundTicketParamNotNullChainFilter implements TrainRefundTicketChainFilter<RefundTicketReqDTO> {
    @Override
    public void handler(RefundTicketReqDTO requestParam) {
        if (StrUtil.isBlank(requestParam.getOrderSn())) {
            throw new RuntimeException("订单号不能为空");
        }
        if (requestParam.getType() == null) {
            throw new RuntimeException("退款类型不能为空");
        }
        if (requestParam.getType().equals(RefundTypeEnum.PARTIAL_REFUND.getType())) {
            if (CollUtil.isEmpty(requestParam.getSubOrderRecordIdReqList())) {
                throw new RuntimeException("部分退款子订单记录集合不能为空");
            }
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
