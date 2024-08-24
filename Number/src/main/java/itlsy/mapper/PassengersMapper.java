package itlsy.mapper;

import itlsy.entry.Passengers;
import itlsy.entry.PassengersExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface PassengersMapper {
    long countByExample(PassengersExample example);

    int deleteByExample(PassengersExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Passengers record);

    int insertSelective(Passengers record);

    List<Passengers> selectByExample(PassengersExample example);

    Passengers selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Passengers record, @Param("example") PassengersExample example);

    int updateByExample(@Param("record") Passengers record, @Param("example") PassengersExample example);

    int updateByPrimaryKeySelective(Passengers record);

    int updateByPrimaryKey(Passengers record);
}