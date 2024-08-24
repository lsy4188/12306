package itlsy.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.protobuf.ServiceException;
import itlsy.context.LoginMemberContext;
import itlsy.dto.OrderStatusReversalDTO;
import itlsy.entry.*;
import itlsy.enums.OrderItemStatusEnum;
import itlsy.enums.OrderStatusEnum;
import itlsy.exception.BusinessException;
import itlsy.feign.UserRemoteService;
import itlsy.feign.dto.UserQueryActualRespDTO;
import itlsy.mapper.OrderItemMapper;
import itlsy.mapper.OrderItemPassengerMapper;
import itlsy.mapper.OrderMapper;
import itlsy.mq.event.DelayCloseOrderEvent;
import itlsy.mq.event.PayResultCallbackOrderEvent;
import itlsy.orderId.OrderIdGeneratorManager;
import itlsy.req.*;
import itlsy.resp.TicketOrderDetailRespDTO;
import itlsy.resp.TicketOrderDetailSelfRespDTO;
import itlsy.resp.TicketOrderPassengerDetailRespDTO;
import itlsy.result.Result;
import itlsy.service.OrderService;
import itlsy.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.OrderByElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderItemPassengerMapper orderItemPassengerMapper;
    @Autowired
    private UserRemoteService userRemoteService;


    @Override
    public TicketOrderDetailRespDTO queryTicketOrderByOrderSn(String orderSn) {
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andOrderSnEqualTo(orderSn);
        Order order = orderMapper.selectByExample(orderExample).get(0);
        TicketOrderDetailRespDTO result = BeanUtil.copyProperties(order, TicketOrderDetailRespDTO.class);
        OrderItemExample orderItemExample = new OrderItemExample();
        orderItemExample.createCriteria().andOrderSnEqualTo(orderSn);
        List<OrderItem> orderItemList = orderItemMapper.selectByExample(orderItemExample);
        result.setPassengerDetails(BeanUtil.copyToList(orderItemList, TicketOrderPassengerDetailRespDTO.class));
        return result;
    }

    @Override
    public PageResponse<TicketOrderDetailRespDTO> pageTicketOrder(TicketOrderPageQueryReqDTO req) {
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria()
                .andUserIdEqualTo(Long.valueOf(req.getUserId()))
                .andStatusIn(buildOrderStatusList(req.getStatusType()));
        orderExample.setOrderByClause("order_time");
        //开始分页
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Order> orders = orderMapper.selectByExample(orderExample);
        PageInfo<Order> orderPageInfo = new PageInfo<>(orders);
        log.info("当前页：{}", orderPageInfo.getPageNum());
        log.info("每页显示条数：{}", orderPageInfo.getPageSize());
        log.info("总页数：{}", orderPageInfo.getPages());
        log.info("总条数：{}", orderPageInfo.getTotal());

        List<TicketOrderDetailRespDTO> newResult = new ArrayList<>();
        for (TicketOrderDetailRespDTO result : BeanUtil.copyToList(orders, TicketOrderDetailRespDTO.class)) {
            OrderItemExample example = new OrderItemExample();
            example.createCriteria().andOrderSnEqualTo(result.getOrderSn());
            List<OrderItem> orderItemDOList = orderItemMapper.selectByExample(example);
            result.setPassengerDetails(BeanUtil.copyToList(orderItemDOList, TicketOrderPassengerDetailRespDTO.class));
            newResult.add(result);
        }
        return PageResponse.<TicketOrderDetailRespDTO>builder()
                .current((long) orderPageInfo.getPageNum())
                .size((long) orderPageInfo.getPageSize())
                .records(newResult)
                .total(orderPageInfo.getTotal())
                .build();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTicketOrder(CancelTicketOrderReqDTO requestParam) {
        String orderSn = requestParam.getOrderSn();
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andOrderSnEqualTo(orderSn);
        Order orderDO = orderMapper.selectByExample(orderExample).get(0);
        if (orderDO == null) {
            throw new RuntimeException("订单不存在");
        } else if (orderDO.getStatus() != OrderStatusEnum.PENDING_PAYMENT.getStatus()) {
            throw new RuntimeException("订单状态不正确");
        }
        // TODO 待添加分布式锁
        Order updateOrder = new Order();
        updateOrder.setStatus(OrderStatusEnum.CLOSED.getStatus());
        int updateResult = orderMapper.updateByExampleSelective(updateOrder, orderExample);
        if (updateResult <= 0) {
            throw new RuntimeException("订单取消失败");
        }
        OrderItem updateOrderItem = new OrderItem();
        updateOrderItem.setStatus(OrderItemStatusEnum.CLOSED.getStatus());
        OrderItemExample itemExample = new OrderItemExample();
        itemExample.createCriteria().andOrderSnEqualTo(orderSn);
        int updateItemResult = orderItemMapper.updateByExampleSelective(updateOrderItem, itemExample);
        if (updateItemResult <= 0) {
            throw new RuntimeException("订单详细取消失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createTicketOrder(TicketOrderCreateReqDTO requestParam) {
        // 通过基因法将用户 ID 融入到订单号
        String orderSn = OrderIdGeneratorManager.generateId(requestParam.getUserId());
        Order orderDO = Order.builder()
                .id(SnowUtil.getSnowflakeNextId())
                .orderSn(orderSn)
                .orderTime(requestParam.getOrderTime())
                .departure(requestParam.getDeparture())
                .departureTime(requestParam.getDepartureTime())
                .ridingDate(requestParam.getRidingDate())
                .arrival(requestParam.getArrival())
                .arrivalTime(requestParam.getArrivalTime())
                .trainNumber(requestParam.getTrainNumber())
                .trainId(requestParam.getTrainId())
                .source(requestParam.getSource())
                .status(OrderStatusEnum.PENDING_PAYMENT.getStatus())
                .username(requestParam.getUsername())
                .userId(String.valueOf(requestParam.getUserId()))
                .payType(0)
                .payTime(new Date())
                .createTime(new Date())
                .updateTime(new Date())
                .delFlag(0)
                .build();
        orderMapper.insert(orderDO);

        List<TicketOrderItemCreateReqDTO> ticketOrderItems = requestParam.getTicketOrderItems();
        List<OrderItem> orderItemDOList = new ArrayList<>();
        List<OrderItemPassenger> orderPassengerRelationDOList = new ArrayList<>();
        ticketOrderItems.forEach(each -> {
            OrderItem orderItemDO = OrderItem.builder()
                    .id(SnowUtil.getSnowflakeNextId())
                    .trainId(requestParam.getTrainId())
                    .seatNumber(each.getSeatNumber())
                    .realName(each.getRealName())
                    .carriageNumber(each.getCarriageNumber())
                    .orderSn(orderSn)
                    .phone(each.getPhone())
                    .seatType(each.getSeatType())
                    .username(requestParam.getUsername())
                    .amount(each.getAmount())
                    .idCard(each.getIdCard())
                    .ticketType(each.getTicketType())
                    .idType(each.getIdType())
                    .userId(String.valueOf(requestParam.getUserId()))
                    .status(OrderItemStatusEnum.PENDING_PAYMENT.getStatus())

                    //使用Aop生成
                    .createTime(new Date())
                    .updateTime(new Date())
                    .delFlag(0)
                    .build();
            orderItemDOList.add(orderItemDO);

            OrderItemPassenger orderItemPassenger = OrderItemPassenger.builder()
                    .id(SnowUtil.getSnowflakeNextId())
                    .orderSn(orderSn)
                    .idType(each.getIdType())
                    .idCard(each.getIdCard())
                    //使用Aop
                    .createTime(new Date())
                    .updateTime(new Date())
                    .delFlag(0)
                    .build();
            orderPassengerRelationDOList.add(orderItemPassenger);
        });
        orderItemMapper.insertBatch(orderItemDOList);
        orderItemPassengerMapper.insertBatch(orderPassengerRelationDOList);

        //集成消息队列(发送 RabbitMQ 延时消息，指定时间后取消订单)
        DelayCloseOrderEvent delayCloseOrderEvent = DelayCloseOrderEvent.builder()
                .trainId(String.valueOf(requestParam.getTrainId()))
                .orderSn(orderSn)
                .departure(requestParam.getDeparture())
                .arrival(requestParam.getArrival())
                .trainPurchaseTicketResults(requestParam.getTicketOrderItems())
                .build();

        return orderSn;
    }

    @Override
    public void statusReversal(OrderStatusReversalDTO requestParam) {
        //校验订单状态
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        Order order = orderMapper.selectByExample(orderExample).get(0);
        if (ObjectUtil.isNull(order)) {
            throw new RuntimeException("订单不存在，请检查相关订单记录");
        } else if (!ObjectUtil.equals(order.getStatus(), OrderStatusEnum.PENDING_PAYMENT.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法进行状态回退");
        }
        //更新订单表
        // TODO 此处要加锁
        Order updateOrder = new Order();
        updateOrder.setStatus(requestParam.getOrderStatus());
        OrderExample example = new OrderExample();
        example.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        int updateResult = orderMapper.updateByExampleSelective(updateOrder, example);
        if (updateResult <= 0) {
            throw new RuntimeException("订单状态反转失败，请稍后再试");
        }

        //更新订单明细表
        OrderItem updataOrderItem = new OrderItem();
        updataOrderItem.setStatus(requestParam.getOrderItemStatus());
        OrderItemExample orderItemExample = new OrderItemExample();
        orderItemExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        int orderItemUpdateResult = orderItemMapper.updateByExampleSelective(updataOrderItem, orderItemExample);
        if (orderItemUpdateResult <= 0) {
            throw new RuntimeException("订单明细表状态反转失败，请稍后再试");
        }
    }

    @Override
    public void payCallbackOrder(PayResultCallbackOrderEvent requestParam) {
        Order order = new Order();
        order.setPayTime(requestParam.getGmtPayment());
        order.setPayType(requestParam.getChannel());
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        int updateResult = orderMapper.updateByExampleSelective(order, orderExample);
        if (updateResult <= 0) {
            throw new RuntimeException("订单支付状态更新失败，请稍后再试");
        }
    }

    @Override
    public List<TicketOrderPassengerDetailRespDTO> queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam) {
        OrderItemExample orderItemExample = new OrderItemExample();
        orderItemExample.createCriteria()
                .andOrderSnEqualTo(requestParam.getOrderSn())
                .andIdIn(requestParam.getOrderItemRecordIds());
        List<OrderItem> orderItemList = orderItemMapper.selectByExample(orderItemExample);
        return BeanUtil.copyToList(orderItemList,TicketOrderPassengerDetailRespDTO.class);
    }


    @Override
    public PageResponse<TicketOrderDetailSelfRespDTO> pageSelfTicketOrder(TicketOrderSelfPageQueryReqDTO requestParam) {
        Result<UserQueryActualRespDTO> userActualResp = userRemoteService.queryActualUserByUsername(LoginMemberContext.getUserName());
        OrderItemExample itemExample = new OrderItemExample();
        itemExample.createCriteria().andUsernameEqualTo(userActualResp.getData().getUsername());
        String idCard = orderItemMapper.selectByExample(itemExample).get(0).getIdCard();
        OrderItemPassengerExample example = new OrderItemPassengerExample();
        example.createCriteria().andIdCardEqualTo(idCard);
        example.setOrderByClause("create_time desc");
        PageHelper.startPage(requestParam.getPage(),requestParam.getSize());
        PageInfo<TicketOrderDetailSelfRespDTO> pagrInfo = new PageInfo<>();
        List<OrderItemPassenger> orderItemPassengers = orderItemPassengerMapper.selectByExample(example);
        ArrayList<TicketOrderDetailSelfRespDTO> list = new ArrayList<>();
        PageResponse<TicketOrderDetailSelfRespDTO> ticketOrderDetailSelfRespDTOPageResponse = new PageResponse<>();
        orderItemPassengers.forEach(item->{
            OrderExample orderExample = new OrderExample();
            orderExample.createCriteria().andOrderSnEqualTo(item.getOrderSn());
            Order order = orderMapper.selectByExample(orderExample).get(0);
            OrderItemExample orderItemExample = new OrderItemExample();
            orderItemExample.createCriteria()
                    .andOrderSnEqualTo(item.getOrderSn())
                    .andIdCardEqualTo(item.getIdCard());
            OrderItem orderItem = orderItemMapper.selectByExample(orderItemExample).get(0);
            TicketOrderDetailSelfRespDTO actualResult = BeanUtil.copyProperties(order, TicketOrderDetailSelfRespDTO.class);
            BeanUtil.copyProperties(orderItem,actualResult);
            list.add(actualResult);
        });
        ticketOrderDetailSelfRespDTOPageResponse.setRecords(list);
        ticketOrderDetailSelfRespDTOPageResponse.setTotal(pagrInfo.getTotal());
        return ticketOrderDetailSelfRespDTOPageResponse;

    }

    private List<Integer> buildOrderStatusList(Integer statusType) {
        List<Integer> result = new ArrayList<>();
        switch (statusType) {
            case 0 -> result = ListUtil.of(
                    OrderStatusEnum.PENDING_PAYMENT.getStatus()
            );
            case 1 -> result = ListUtil.of(
                    OrderStatusEnum.ALREADY_PAID.getStatus(),
                    OrderStatusEnum.PARTIAL_REFUND.getStatus(),
                    OrderStatusEnum.FULL_REFUND.getStatus()
            );
            case 2 -> result = ListUtil.of(
                    OrderStatusEnum.COMPLETED.getStatus()
            );
        }
        return result;
    }
}
