package itlsy.payId;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *支付 ID 全局唯一生成器管理
 * TODO 待修改
 */
public class PayIdGeneratorManager  {
    /**
     * 生成支付全局唯一流水号
     *
     * @param orderSn 订单号
     * @return 支付流水号
     *
     */
    //445598607446016383680
    public static String generateId(String orderSn) {
        DistributedIdGenerator DISTRIBUTED_ID_GENERATOR = new DistributedIdGenerator(0L);
        String substring = orderSn.substring(orderSn.length() - 6);
        return DISTRIBUTED_ID_GENERATOR.generateId() + substring;
    }

}
