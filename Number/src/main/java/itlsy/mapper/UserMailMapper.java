package itlsy.mapper;

import itlsy.entry.UserMail;
import itlsy.entry.UserMailExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMailMapper {
    long countByExample(UserMailExample example);

    int deleteByExample(UserMailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UserMail record);

    int insertSelective(UserMail record);

    List<UserMail> selectByExample(UserMailExample example);

    UserMail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UserMail record, @Param("example") UserMailExample example);

    int updateByExample(@Param("record") UserMail record, @Param("example") UserMailExample example);

    int updateByPrimaryKeySelective(UserMail record);

    int updateByPrimaryKey(UserMail record);
}