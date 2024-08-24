package itlsy.mapper;

import itlsy.entry.TrainStationPrice;
import itlsy.entry.TrainStationPriceExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrainStationPriceMapper {
    long countByExample(TrainStationPriceExample example);

    int deleteByExample(TrainStationPriceExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TrainStationPrice record);

    int insertSelective(TrainStationPrice record);

    List<TrainStationPrice> selectByExample(TrainStationPriceExample example);

    TrainStationPrice selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TrainStationPrice record, @Param("example") TrainStationPriceExample example);

    int updateByExample(@Param("record") TrainStationPrice record, @Param("example") TrainStationPriceExample example);

    int updateByPrimaryKeySelective(TrainStationPrice record);

    int updateByPrimaryKey(TrainStationPrice record);
}