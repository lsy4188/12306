package itlsy.mapper;

import itlsy.entry.PayDO;
import itlsy.entry.PayDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PayMapper {
    long countByExample(PayDOExample example);

    int deleteByExample(PayDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PayDO record);

    int insertSelective(PayDO record);

    List<PayDO> selectByExample(PayDOExample example);

    PayDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PayDO record, @Param("example") PayDOExample example);

    int updateByExample(@Param("record") PayDO record, @Param("example") PayDOExample example);

    int updateByPrimaryKeySelective(PayDO record);

    int updateByPrimaryKey(PayDO record);
}