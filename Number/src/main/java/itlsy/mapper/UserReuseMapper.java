package itlsy.mapper;

import itlsy.entry.UserReuse;
import itlsy.entry.UserReuseExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserReuseMapper {
    long countByExample(UserReuseExample example);

    int deleteByExample(UserReuseExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UserReuse record);

    int insertSelective(UserReuse record);

    List<UserReuse> selectByExample(UserReuseExample example);

    UserReuse selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UserReuse record, @Param("example") UserReuseExample example);

    int updateByExample(@Param("record") UserReuse record, @Param("example") UserReuseExample example);

    int updateByPrimaryKeySelective(UserReuse record);

    int updateByPrimaryKey(UserReuse record);
}