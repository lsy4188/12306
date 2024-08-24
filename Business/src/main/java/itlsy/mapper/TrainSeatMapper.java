package itlsy.mapper;

import itlsy.dto.SeatTypeCountDTO;
import itlsy.entry.TrainSeat;
import itlsy.entry.TrainSeatExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrainSeatMapper {
    long countByExample(TrainSeatExample example);

    int deleteByExample(TrainSeatExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TrainSeat record);

    int insertSelective(TrainSeat record);

    List<TrainSeat> selectByExample(TrainSeatExample example);

    TrainSeat selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TrainSeat record, @Param("example") TrainSeatExample example);

    int updateByExample(@Param("record") TrainSeat record, @Param("example") TrainSeatExample example);

    int updateByPrimaryKeySelective(TrainSeat record);

    int updateByPrimaryKey(TrainSeat record);

    /**
     * 获取列车车厢余票集合
     */
    List<Integer> listSeatRemainingTicket(@Param("trainSeat") TrainSeat trainSeat, @Param("trainCarriageList") List<String> trainCarriageList);
    /**
     * 获取列车 startStation 到 endStation 区间可用座位数量
     */
    List<SeatTypeCountDTO> listSeatTypeCount(@Param("trainId") Long trainId, @Param("startStation") String startStation, @Param("endStation") String endStation, @Param("seatTypes") List<Integer> seatTypes);
}