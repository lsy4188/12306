package itlsy.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import itlsy.dto.OrderItemStatusReversalDTO;
import itlsy.entry.Order;
import itlsy.entry.OrderExample;
import itlsy.entry.OrderItem;
import itlsy.entry.OrderItemExample;
import itlsy.enums.OrderCanalErrorCodeEnum;
import itlsy.exception.ServiceException;
import itlsy.mapper.OrderItemMapper;
import itlsy.mapper.OrderMapper;
import itlsy.service.OrderItemService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void orderItemStatusReversal(OrderItemStatusReversalDTO requestParam) {
        //更新订单表
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria()
                .andOrderSnEqualTo(requestParam.getOrderSn());
        Order order = orderMapper.selectByExample(orderExample).get(0);
        if (ObjectUtil.isNull(order)){
            throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_UNKNOWN_ERROR);
        }
        RLock lock = redissonClient.getLock(StrBuilder.create("order:status-reversal:order_sn_").append(requestParam.getOrderSn()).toString());
        if (!lock.tryLock()){
            log.warn("订单重复修改状态，状态反转请求参数：{}", JSONUtil.toJsonStr(requestParam));
        }
        try {
            Order updateOrderDO = new Order();
            updateOrderDO.setStatus(requestParam.getOrderStatus());
            OrderExample example = new OrderExample();
            example.createCriteria()
                    .andOrderSnEqualTo(requestParam.getOrderSn());
            int orderUpdateResult = orderMapper.updateByExampleSelective(updateOrderDO, example);
            if (orderUpdateResult<0){
                throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_STATUS_REVERSAL_ERROR);
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
                            throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_ITEM_STATUS_REVERSAL_ERROR);
                        }
                    });
                }
            }
        } finally {
            lock.unlock();

        }
    }
}
