package itlsy.handler.filter.query;

import cn.hutool.core.util.ObjectUtil;
import itlsy.exception.ClientException;
import itlsy.req.TicketPageQueryReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 查询列车车票流程过滤器之基础数据验证
 */
@Component
@RequiredArgsConstructor
public class TrainTicketQueryParamBaseVerifyChainFilter implements TrainTicketQueryChainFilter<TicketPageQueryReqDTO> {

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void handler(TicketPageQueryReqDTO requestParam) {
        if (requestParam.getDepartureDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(LocalDate.now())){
            throw new ClientException("出发日期不能小于当前日期");
        }
        if (ObjectUtil.equals(requestParam.getFromStation(),requestParam.getToStation())){
            throw new ClientException("出发站和到达站不能相同");
        }
    }
}
