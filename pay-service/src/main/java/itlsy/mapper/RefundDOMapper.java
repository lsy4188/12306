package itlsy.mapper;

import itlsy.entry.RefundDO;
import itlsy.entry.RefundDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefundDOMapper {
    long countByExample(RefundDOExample example);

    int deleteByExample(RefundDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RefundDO record);

    int insertSelective(RefundDO record);

    List<RefundDO> selectByExample(RefundDOExample example);

    RefundDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") RefundDO record, @Param("example") RefundDOExample example);

    int updateByExample(@Param("record") RefundDO record, @Param("example") RefundDOExample example);

    int updateByPrimaryKeySelective(RefundDO record);

    int updateByPrimaryKey(RefundDO record);
}