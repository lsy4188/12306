package itlsy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SeatClassDTO {
    /**
     * 席别类型
     */
    private Integer type;

    /**
     * 席别数量
     */
    private Integer quantity;

    /**
     * 席别价格
     */
    private BigDecimal price;

    /**
     * 席别候补标识
     */
    private Boolean candidate;
}
