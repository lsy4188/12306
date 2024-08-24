package itlsy.mapper;

import itlsy.entry.UserPhone;
import itlsy.entry.UserPhoneExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserPhoneMapper {
    long countByExample(UserPhoneExample example);

    int deleteByExample(UserPhoneExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UserPhone record);

    int insertSelective(UserPhone record);

    List<UserPhone> selectByExample(UserPhoneExample example);

    UserPhone selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UserPhone record, @Param("example") UserPhoneExample example);

    int updateByExample(@Param("record") UserPhone record, @Param("example") UserPhoneExample example);

    int updateByPrimaryKeySelective(UserPhone record);

    int updateByPrimaryKey(UserPhone record);
}