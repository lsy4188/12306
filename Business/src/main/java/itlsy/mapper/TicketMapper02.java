package itlsy.mapper;

import itlsy.entry.Ticket02;
import itlsy.entry.TicketExample02;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 旧版本
 */
@Mapper

public interface TicketMapper02 {
    long countByExample(TicketExample02 example);

    int deleteByExample(TicketExample02 example);

    int deleteByPrimaryKey(Long id);

    int insert(Ticket02 record);

    int insertSelective(Ticket02 record);

    List<Ticket02> selectByExample(TicketExample02 example);

    Ticket02 selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Ticket02 record, @Param("example") TicketExample02 example);

    int updateByExample(@Param("record") Ticket02 record, @Param("example") TicketExample02 example);

    int updateByPrimaryKeySelective(Ticket02 record);

    int updateByPrimaryKey(Ticket02 record);
}