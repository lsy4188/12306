package itlsy.resp;

import itlsy.dto.TicketListDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TicketQueryRespDTO {
    /**
     * 车次集合数据
     */
    private List<TicketListDTO> trainList;

    /**
     * 车次类型：D-动车 Z-直达 复兴号等
     */
    private List<Integer> trainBrandList;

    /**
     * 出发车站
     */
    private List<String> departureStationList;

    /**
     * 到达车站
     */
    private List<String> arrivalStationList;

    /**
     * 车次席别
     */
    private List<Integer> seatClassTypeList;

}
