package itlsy.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.protobuf.ServiceException;
import itlsy.dto.OrderItemStatusReversalDTO;
import itlsy.entry.Order;
import itlsy.entry.OrderExample;
import itlsy.entry.OrderItem;
import itlsy.entry.OrderItemExample;
import itlsy.mapper.OrderItemMapper;
import itlsy.mapper.OrderMapper;
import itlsy.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;
    @Override
    public void orderItemStatusReversal(OrderItemStatusReversalDTO requestParam) {
        //更新订单表
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria()
                .andOrderSnEqualTo(requestParam.getOrderSn());
        Order order = orderMapper.selectByExample(orderExample).get(0);
        if (ObjectUtil.isNull(order)){
            throw new RuntimeException("订单不存在，请检查相关订单记录");
        }
        //TODO 待加锁
        Order updateOrderDO = new Order();
        updateOrderDO.setStatus(requestParam.getOrderStatus());
        OrderExample example = new OrderExample();
        example.createCriteria()
                .andOrderSnEqualTo(requestParam.getOrderSn());
        int orderUpdateResult = orderMapper.updateByExampleSelective(updateOrderDO, example);
        if (orderUpdateResult<0){
            throw new RuntimeException("订单状态反转失败，请稍后再试");
        }
        //更新订单明细表
        if (CollUtil.isNotEmpty(requestParam.getOrderItemDOList())){
            List<OrderItem> orderItemDOList = requestParam.getOrderItemDOList();
            if (CollUtil.isNotEmpty(orderItemDOList)){
                orderItemDOList.forEach(orderItem -> {
                    OrderItem item = new OrderItem();
                    item.setStatus(requestParam.getOrderItemStatus());
                    OrderItemExample orderItemExample = new OrderItemExample();
                    orderItemExample.createCriteria()
                            .andOrderSnEqualTo(requestParam.getOrderSn())
                            .andRealNameEqualTo(orderItem.getRealName());
                    int orderItemUpdateResult = orderItemMapper.updateByExampleSelective(item, orderItemExample);
                    if (orderItemUpdateResult <= 0) {
                        throw new RuntimeException("子订单状态反转失败，请稍后再试");
                    }
                });
            }
        }
    }
}
