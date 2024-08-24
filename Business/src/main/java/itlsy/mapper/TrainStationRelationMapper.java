package itlsy.mapper;

import itlsy.entry.TrainStationRelation;
import itlsy.entry.TrainStationRelationExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrainStationRelationMapper {
    long countByExample(TrainStationRelationExample example);

    int deleteByExample(TrainStationRelationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TrainStationRelation record);

    int insertSelective(TrainStationRelation record);

    List<TrainStationRelation> selectByExample(TrainStationRelationExample example);

    TrainStationRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TrainStationRelation record, @Param("example") TrainStationRelationExample example);

    int updateByExample(@Param("record") TrainStationRelation record, @Param("example") TrainStationRelationExample example);

    int updateByPrimaryKeySelective(TrainStationRelation record);

    int updateByPrimaryKey(TrainStationRelation record);
}