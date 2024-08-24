package itlsy.mapper;

import itlsy.entry.TrainSeat02;
import itlsy.entry.TrainSeatExample02;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 旧版本
 */
@Mapper
public interface TrainSeatMapper02 {
    long countByExample(TrainSeatExample02 example);

    int deleteByExample(TrainSeatExample02 example);

    int deleteByPrimaryKey(Long id);

    int insert(TrainSeat02 record);

    int insertSelective(TrainSeat02 record);

    List<TrainSeat02> selectByExample(TrainSeatExample02 example);

    TrainSeat02 selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TrainSeat02 record, @Param("example") TrainSeatExample02 example);

    int updateByExample(@Param("record") TrainSeat02 record, @Param("example") TrainSeatExample02 example);

    int updateByPrimaryKeySelective(TrainSeat02 record);

    int updateByPrimaryKey(TrainSeat02 record);
}