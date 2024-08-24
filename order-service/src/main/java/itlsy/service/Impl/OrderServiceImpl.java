package itlsy.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import itlsy.context.LoginMemberContext;
import itlsy.dto.OrderStatusReversalDTO;
import itlsy.entry.*;
import itlsy.enums.OrderCanalErrorCodeEnum;
import itlsy.enums.OrderItemStatusEnum;
import itlsy.enums.OrderStatusEnum;
import itlsy.exception.ClientException;
import itlsy.exception.ServiceException;
import itlsy.feign.UserRemoteService;
import itlsy.feign.dto.UserQueryActualRespDTO;
import itlsy.mapper.OrderItemMapper;
import itlsy.mapper.OrderItemPassengerMapper;
import itlsy.mapper.OrderMapper;
import itlsy.mq.event.DelayCloseOrderEvent;
import itlsy.mq.event.PayResultCallbackOrderEvent;
import itlsy.mq.produce.DelayCloseOrderSendProduce;
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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
    @Autowired
    private DelayCloseOrderSendProduce delayCloseOrderSendProduce;
    @Autowired
    private RedissonClient redissonClient;


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
                .andStatusIn(buildOrderStatusList(req));
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
    public String createTicketOrder(TicketOrderCreateReqDTO requestParam) {
        // 通过基因法将用户 ID 融入到订单号
        String orderSn = OrderIdGeneratorManager.generateId(requestParam.getUserId());
        // 创建订单
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

        // 创建订单明细表
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


        try {
            //集成消息队列(发送 RabbitMQ 延时消息，指定时间后取消订单)
            DelayCloseOrderEvent delayCloseOrderEvent = DelayCloseOrderEvent.builder()
                    .trainId(String.valueOf(requestParam.getTrainId()))
                    .orderSn(orderSn)
                    .departure(requestParam.getDeparture())
                    .arrival(requestParam.getArrival())
                    .trainPurchaseTicketResults(requestParam.getTicketOrderItems())
                    .build();
            delayCloseOrderSendProduce.sendMessage(delayCloseOrderEvent);
        } catch (Exception e) {
            log.error("延迟关闭订单消息队列发送错误，请求参数：{}", JSON.toJSONString(requestParam), e);
            throw e;
        }
        return orderSn;
    }

    @Override
    public void statusReversal(OrderStatusReversalDTO requestParam) {
        //校验订单状态
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        Order order = orderMapper.selectByExample(orderExample).get(0);
        if (ObjectUtil.isNull(order)) {
            throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_UNKNOWN_ERROR);
        } else if (!ObjectUtil.equals(order.getStatus(), OrderStatusEnum.PENDING_PAYMENT.getStatus())) {
            throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_STATUS_ERROR);
        }
        //更新订单表
        RLock lock = redissonClient.getLock(StrBuilder.create("order:status-reversal:order_sn_").append(requestParam.getOrderSn()).toString());
        if (!lock.tryLock()){
            log.warn("订单重复修改状态，状态反转请求参数：{}", JSONUtil.toJsonStr(requestParam));
        }
        try {
            Order updateOrder = new Order();
            updateOrder.setStatus(requestParam.getOrderStatus());
            OrderExample example = new OrderExample();
            example.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
            int updateResult = orderMapper.updateByExampleSelective(updateOrder, example);
            if (updateResult <= 0) {
                throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_DELETE_ERROR);
            }
            //更新订单明细表
            OrderItem updataOrderItem = new OrderItem();
            updataOrderItem.setStatus(requestParam.getOrderItemStatus());
            OrderItemExample orderItemExample = new OrderItemExample();
            orderItemExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
            int orderItemUpdateResult = orderItemMapper.updateByExampleSelective(updataOrderItem, orderItemExample);
            if (orderItemUpdateResult <= 0) {
                throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_STATUS_REVERSAL_ERROR);
            }
        } finally {
            lock.unlock();
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
            throw new ServiceException("订单支付状态更新失败，请稍后再试");
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeTickOrder(CancelTicketOrderReqDTO requestParam) {
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria()
                .andOrderSnEqualTo(requestParam.getOrderSn());
        Order order = orderMapper.selectByExample(orderExample).get(0);
        if (ObjectUtil.isNull(order)||order.getStatus().equals(OrderStatusEnum.PENDING_PAYMENT.getStatus())){
            return false;
        }
        // 原则上订单关闭和订单取消这两个方法可以复用，为了区分未来考虑到的场景，这里对方法进行拆分但复用逻辑
        return cancelTicketOrder(requestParam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTicketOrder(CancelTicketOrderReqDTO requestParam) {
        String orderSn = requestParam.getOrderSn();
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andOrderSnEqualTo(orderSn);
        Order orderDO = orderMapper.selectByExample(orderExample).get(0);
        if (orderDO == null) {
            throw new ServiceException("订单不存在");
        } else if (orderDO.getStatus() != OrderStatusEnum.PENDING_PAYMENT.getStatus()) {
            throw new ServiceException("订单状态不正确");
        }
        RLock lock = redissonClient.getLock(StrBuilder.create("order:canal:order_sn_").append(requestParam.getOrderSn()).toString());
        if (!lock.tryLock()){
            throw new ClientException(OrderCanalErrorCodeEnum.ORDER_CANAL_REPETITION_ERROR);
        }
        try {
            Order updateOrder = new Order();
            updateOrder.setStatus(OrderStatusEnum.CLOSED.getStatus());
            int updateResult = orderMapper.updateByExampleSelective(updateOrder, orderExample);
            if (updateResult <= 0) {
                throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_ERROR);
            }
            OrderItem updateOrderItem = new OrderItem();
            updateOrderItem.setStatus(OrderItemStatusEnum.CLOSED.getStatus());
            OrderItemExample itemExample = new OrderItemExample();
            itemExample.createCriteria().andOrderSnEqualTo(orderSn);
            int updateItemResult = orderItemMapper.updateByExampleSelective(updateOrderItem, itemExample);
            if (updateItemResult <= 0) {
                throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_ERROR);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    private List<Integer> buildOrderStatusList(TicketOrderPageQueryReqDTO requestParam) {
        List<Integer> result = new ArrayList<>();
        switch (requestParam.getStatusType()) {
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
