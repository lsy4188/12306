package itlsy.orderId;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 订单 ID 全局唯一生成器管理
 * TODO 待修改
 */
public final class OrderIdGeneratorManager  {
    public static String generateId(long userId) {
        DistributedIdGenerator DISTRIBUTED_ID_GENERATOR = new DistributedIdGenerator(0L);
        return DISTRIBUTED_ID_GENERATOR.generateId()+ String.valueOf(userId%1000000);
    }
}
