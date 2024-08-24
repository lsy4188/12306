package itlsy.mapper;

import itlsy.entry.OrderItemPassenger;
import itlsy.entry.OrderItemPassengerExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderItemPassengerMapper {
    long countByExample(OrderItemPassengerExample example);

    int deleteByExample(OrderItemPassengerExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OrderItemPassenger record);

    int insertSelective(OrderItemPassenger record);

    List<OrderItemPassenger> selectByExample(OrderItemPassengerExample example);

    OrderItemPassenger selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") OrderItemPassenger record, @Param("example") OrderItemPassengerExample example);

    int updateByExample(@Param("record") OrderItemPassenger record, @Param("example") OrderItemPassengerExample example);

    int updateByPrimaryKeySelective(OrderItemPassenger record);

    int updateByPrimaryKey(OrderItemPassenger record);

    void insertBatch(@Param("orderPassengerRelationDOList") List<OrderItemPassenger> orderPassengerRelationDOList);
}