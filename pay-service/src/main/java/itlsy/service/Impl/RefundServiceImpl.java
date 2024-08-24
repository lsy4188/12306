package itlsy.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.protobuf.ServiceException;
import itlsy.convert.RefundRequestConvert;
import itlsy.dto.RefundCreateDTO;
import itlsy.entry.PayDO;
import itlsy.entry.PayDOExample;
import itlsy.entry.RefundDO;
import itlsy.entry.RefundDOExample;
import itlsy.enums.RefundTypeEnum;
import itlsy.enums.TradeStatusEnum;
import itlsy.feign.TicketOrderRemoteService;
import itlsy.feign.dto.TicketOrderDetailRespDTO;
import itlsy.mapper.PayMapper;
import itlsy.mapper.RefundDOMapper;
import itlsy.mq.event.RefundResultCallbackOrderEvent;
import itlsy.mq.produce.RefundResultCallbackOrderSendProduce;
import itlsy.req.RefundCommand;
import itlsy.req.RefundReqDTO;
import itlsy.req.RefundRequest;
import itlsy.resp.RefundRespDTO;
import itlsy.resp.RefundResponse;
import itlsy.result.Result;
import itlsy.service.RefundService;
import itlsy.strategy.AbstractStrategyChoose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 退款接口层实现
 */
@Service
@Slf4j
public class RefundServiceImpl implements RefundService {
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private RefundDOMapper refundDOMapper;
    @Autowired
    private TicketOrderRemoteService ticketOrderRemoteService;
    @Autowired
    private AbstractStrategyChoose abstractStrategyChoose;
    @Autowired
    private RefundResultCallbackOrderSendProduce refundResultCallbackOrderSendProduce;

    @Override
    @Transactional
    public RefundRespDTO commonRefund(RefundReqDTO requestParam) {
        RefundRespDTO refundRespDTO = null;
        PayDOExample payDOExample = new PayDOExample();
        payDOExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        PayDO payDO = payMapper.selectByExample(payDOExample).get(0);
        if (ObjectUtil.isNull(payDO)) {
            log.error("支付单不存在，orderSn：{}", requestParam.getOrderSn());
            throw new RuntimeException("支付单不存在");
        }
        payDO.setPayAmount(payDO.getTotalAmount() - requestParam.getRefundAmount());
        //创建退款单
        RefundCreateDTO refundCreateDTO = BeanUtil.copyProperties(requestParam, RefundCreateDTO.class);
        refundCreateDTO.setPaySn(payDO.getPaySn());
        createRefund(refundCreateDTO);
        /**
         * {@link AliRefundNativeHandler}
         */
        // 策略模式：通过策略模式封装退款渠道和退款场景，用户退款时动态选择对应的退款组件
        RefundCommand refundCommand = BeanUtil.copyProperties(payDO, RefundCommand.class);
        refundCommand.setPayAmount(new BigDecimal(requestParam.getRefundAmount()));
        RefundRequest refundRequest = RefundRequestConvert.command2RefundRequest(refundCommand);
        RefundResponse result = abstractStrategyChoose.chooseAndExecuteResp(refundRequest.buildMark(), refundRequest);
        payDO.setStatus(result.getStatus());
        PayDOExample example = new PayDOExample();
        example.createCriteria().andOrderSnEqualTo(refundRequest.getOrderSn());
        int updateResult = payMapper.updateByExampleSelective(payDO, example);
        if (updateResult <= 0) {
            log.error("更新支付单状态失败，orderSn：{}", refundRequest.getOrderSn());
            throw new RuntimeException("更新支付单状态失败");
        }
        RefundDOExample refundDOExample = new RefundDOExample();
        refundDOExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        RefundDO refundDO = new RefundDO();
        refundDO.setTradeNo(result.getTradeNo());
        refundDO.setStatus(result.getStatus());
        int refundUpdateResult = refundDOMapper.updateByExampleSelective(refundDO, refundDOExample);
        if (refundUpdateResult <= 0) {
            log.error("更新退款单状态失败，orderSn：{}", requestParam.getOrderSn());
            throw new RuntimeException("更新退款单状态失败");
        }

        // 退款成功，回调订单服务告知退款结果，修改订单流转状态
        if (ObjectUtil.equals(result.getStatus(), TradeStatusEnum.TRADE_CLOSED.tradeCode())) {
            RefundResultCallbackOrderEvent refundResultCallbackOrderEvent = RefundResultCallbackOrderEvent.builder()
                    .orderSn(requestParam.getOrderSn())
                    .refundTypeEnum(requestParam.getRefundTypeEnum())
                    .partialRefundTicketDetailList(requestParam.getRefundDetailReqDTOList())
                    .build();
            refundResultCallbackOrderSendProduce.sendMessage(refundResultCallbackOrderEvent);
        }
        //TODO 暂时返回空实体
        return refundRespDTO;
    }

    @Override
    public RefundRespDTO commonRefundV2(RefundReqDTO requestParam) {
        RefundRespDTO refundRespDTO = null;
        PayDOExample payDOExample = new PayDOExample();
        payDOExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        PayDO payDO = payMapper.selectByExample(payDOExample).get(0);
        if (ObjectUtil.isNull(payDO)) {
            log.error("支付单不存在，orderSn：{}", requestParam.getOrderSn());
            throw new RuntimeException("支付单不存在");
        }
        payDO.setPayAmount(payDO.getTotalAmount() - requestParam.getRefundAmount());

        log.debug(payDO.getPayAmount() + "");

        //创建退款记录表
        RefundCreateDTO refundCreateDTO = BeanUtil.copyProperties(requestParam, RefundCreateDTO.class);
        refundCreateDTO.setPaySn(payDO.getPaySn());
        createRefund(refundCreateDTO);

        //更新支付单状态
        RefundCommand refundCommand = BeanUtil.copyProperties(payDO, RefundCommand.class);
        refundCommand.setPayAmount(new BigDecimal(requestParam.getRefundAmount()));
        RefundRequest refundRequest = RefundRequestConvert.command2RefundRequest(refundCommand);
        payDO.setStatus(TradeStatusEnum.TRADE_CLOSED.tradeCode());
        PayDOExample example = new PayDOExample();
        example.createCriteria().andOrderSnEqualTo(refundRequest.getOrderSn());
        int updateResult = payMapper.updateByExampleSelective(payDO, example);
        if (updateResult <= 0) {
            log.error("更新支付单状态失败，orderSn：{}", refundRequest.getOrderSn());
            throw new RuntimeException("更新支付单状态失败");
        }

        //更新退款记录表状态
        RefundDOExample refundDOExample = new RefundDOExample();
        refundDOExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        RefundDO refundDO = new RefundDO();
        refundDO.setTradeNo(RefundTypeEnum.FULL_REFUND.getName());
        refundDO.setStatus(TradeStatusEnum.TRADE_CLOSED.tradeCode());
        int refundUpdateResult = refundDOMapper.updateByExampleSelective(refundDO, refundDOExample);
        if (refundUpdateResult <= 0) {
            log.error("更新退款单状态失败，orderSn：{}", requestParam.getOrderSn());
            throw new RuntimeException("更新退款单状态失败");
        }

        // 退款成功，回调订单服务告知退款结果，修改订单流转状态
        RefundResultCallbackOrderEvent refundResultCallbackOrderEvent = RefundResultCallbackOrderEvent.builder()
                .orderSn(requestParam.getOrderSn())
                .refundTypeEnum(requestParam.getRefundTypeEnum())
                .partialRefundTicketDetailList(requestParam.getRefundDetailReqDTOList())
                .build();
        refundResultCallbackOrderSendProduce.sendMessage(refundResultCallbackOrderEvent);

        //TODO 暂时返回空实体
        return refundRespDTO;
    }

    private void createRefund(RefundCreateDTO refundCreateDTO) {
        //查订单表和订单明细表
        Result<TicketOrderDetailRespDTO> queryTicketResult = ticketOrderRemoteService.queryTicketOrderByOrderSn(refundCreateDTO.getOrderSn());

        if (!queryTicketResult.isSuccess() && ObjectUtil.isNull(queryTicketResult.getData())) {
            throw new RuntimeException("订单不存在");
        }
        TicketOrderDetailRespDTO data = queryTicketResult.getData();
        refundCreateDTO.getRefundDetailReqDTOList().forEach(item -> {
            //创建退款记录表
            RefundDO refundDO = new RefundDO();
            refundDO.setPaySn(refundCreateDTO.getPaySn());
            refundDO.setOrderSn(refundCreateDTO.getOrderSn());
            refundDO.setTrainId(data.getTrainId());
            refundDO.setTrainNumber(data.getTrainNumber());
            refundDO.setDeparture(data.getDeparture());
            refundDO.setArrival(data.getArrival());
            refundDO.setDepartureTime(data.getDepartureTime());
            refundDO.setArrivalTime(data.getArrivalTime());
            refundDO.setRidingDate(data.getRidingDate());
            refundDO.setSeatType(item.getSeatType());
            refundDO.setIdType(item.getIdType());
            refundDO.setIdCard(item.getIdCard());
            refundDO.setRealName(item.getRealName());
            refundDO.setRefundTime(new Date());
            refundDO.setAmount(item.getAmount());
            refundDO.setUserId(item.getUserId());
            refundDO.setUsername(item.getUsername());
            refundDOMapper.insert(refundDO);
        });
    }
}
