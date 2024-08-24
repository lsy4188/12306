package itlsy.mapper;

import itlsy.entry.TrainStation02;
import itlsy.entry.TrainStationExample02;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 旧版本
 */
@Mapper
public interface TrainStationMapper02 {
    long countByExample(TrainStationExample02 example);

    int deleteByExample(TrainStationExample02 example);

    int deleteByPrimaryKey(Long id);

    int insert(TrainStation02 record);

    int insertSelective(TrainStation02 record);

    List<TrainStation02> selectByExample(TrainStationExample02 example);

    TrainStation02 selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TrainStation02 record, @Param("example") TrainStationExample02 example);

    int updateByExample(@Param("record") TrainStation02 record, @Param("example") TrainStationExample02 example);

    int updateByPrimaryKeySelective(TrainStation02 record);

    int updateByPrimaryKey(TrainStation02 record);
}