package itlsy.mapper.cust;

import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

@Mapper
public interface DailyTrainTicketMapperCust {
    public void updateCountBySell(
            Date date,
            String trainCode,
            String seatTypeCode,
            Integer minStartIndex,
            Integer maxStartIndex,
            Integer minEndIndex,
            Integer maxEndIndex);

}
