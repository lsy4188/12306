package itlsy.mapper;

import itlsy.entry.UserDeletion;
import itlsy.entry.UserDeletionExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDeletionMapper {
    long countByExample(UserDeletionExample example);

    int deleteByExample(UserDeletionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UserDeletion record);

    int insertSelective(UserDeletion record);

    List<UserDeletion> selectByExample(UserDeletionExample example);

    UserDeletion selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UserDeletion record, @Param("example") UserDeletionExample example);

    int updateByExample(@Param("record") UserDeletion record, @Param("example") UserDeletionExample example);

    int updateByPrimaryKeySelective(UserDeletion record);

    int updateByPrimaryKey(UserDeletion record);
}