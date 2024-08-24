package itlsy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 站点路线实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteDTO {
    /**
     * 出发站点
     */
    private String startStation;

    /**
     * 目的站点
     */
    private String endStation;
}
