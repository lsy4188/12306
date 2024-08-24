package itlsy.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import itlsy.DistributedCache;
import itlsy.UserContext;
import itlsy.constant.RedisKeyConstant;
import itlsy.constant.Userconstant;
import itlsy.context.LoginMemberContext;
import itlsy.dto.PayCallbackReqDTO;
import itlsy.entry.PayDO;
import itlsy.entry.PayDOExample;
import itlsy.enums.PayChannelEnum;
import itlsy.enums.TradeStatusEnum;
import itlsy.exception.ServiceException;
import itlsy.mapper.PayMapper;
import itlsy.mq.event.PayResultCallbackOrderEvent;
import itlsy.mq.produce.PayResultCallbackOrderSendProduce;
import itlsy.payId.PayIdGeneratorManager;
import itlsy.req.PayRequest;
import itlsy.resp.PayInfoRespDTO;
import itlsy.resp.PayRespDTO;
import itlsy.resp.PayResponse;
import itlsy.service.PayService;
import itlsy.strategy.AbstractStrategyChoose;
import itlsy.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
//TODO 修改
public class PayServiceImpl implements PayService {

    @Autowired
    private AbstractStrategyChoose abstractStrategyChoose;

    @Autowired
    private PayMapper payMapper;

    @Autowired
    private PayResultCallbackOrderSendProduce payResultCallbackOrderSendProduce;

    @Autowired
    private DistributedCache distributedCache;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayRespDTO commonPay(PayRequest requestParam) {
        PayRespDTO cacheResult = distributedCache.get(RedisKeyConstant.ORDER_PAY_RESULT_INFO + requestParam.getOrderSn(), PayRespDTO.class);
        if (cacheResult!=null){
            return cacheResult;
        }
        /**
         * {@link AliPayNativeHandler}
         */
        // 策略模式：通过策略模式封装支付渠道和支付场景，用户支付时动态选择对应的支付组件
        PayResponse result = abstractStrategyChoose.chooseAndExecuteResp(requestParam.buildMark(), requestParam);
        PayDO insertPay = BeanUtil.copyProperties(requestParam, PayDO.class);
        String paySn = PayIdGeneratorManager.generateId(requestParam.getOrderSn());
        insertPay.setId(SnowUtil.getSnowflakeNextId());
        insertPay.setPaySn(paySn);
        insertPay.setStatus(TradeStatusEnum.WAIT_BUYER_PAY.tradeCode());
        insertPay.setTotalAmount(requestParam.getTotalAmount().multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
        int insert = payMapper.insert(insertPay);
        if (insert<=0){
            log.error("支付单创建失败，支付聚合根：{}", JSON.toJSONString(requestParam));
            throw new ServiceException("支付单创建失败");
        }
        distributedCache.put(RedisKeyConstant.ORDER_PAY_RESULT_INFO + requestParam.getOrderSn(), JSONUtil.toJsonStr(result),10, TimeUnit.MINUTES);
        return BeanUtil.copyProperties(result, PayRespDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void callbackPay(PayCallbackReqDTO requestParam) {
        PayDOExample payDOExample = new PayDOExample();
        payDOExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        PayDO payDO = payMapper.selectByExample(payDOExample).get(0);
        if (ObjectUtil.isEmpty(payDO)){
            log.error("支付单不存在，orderRequestId：{}", requestParam.getOrderRequestId());
            throw new ServiceException("支付单不存在");
        }
        payDO.setTradeNo(requestParam.getTradeNo());
        payDO.setStatus(requestParam.getStatus());
        payDO.setPayAmount(requestParam.getPayAmount());
        payDO.setGmtPayment(requestParam.getGmtPayment());
        PayDOExample example = new PayDOExample();
        example.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        int result = payMapper.updateByExampleSelective(payDO, example);
        if (result<=0){
            log.error("修改支付单支付结果失败，支付单信息：{}", JSON.toJSONString(payDO));
            throw new ServiceException("修改支付单支付结果失败");
        }
        // 交易成功，回调订单服务告知支付结果，修改订单流转状态
        if (ObjectUtil.equals(requestParam.getStatus(),TradeStatusEnum.TRADE_SUCCESS.tradeCode())){
          payResultCallbackOrderSendProduce.sendMessage(BeanUtil.copyProperties(payDO, PayResultCallbackOrderEvent.class));
        }
    }

    @Override
    public PayInfoRespDTO getPayInfoByOrderSn(String orderSn) {
        PayDOExample payExample = new PayDOExample();
        payExample.createCriteria().andOrderSnEqualTo(orderSn);
        PayDO payDO = payMapper.selectByExample(payExample).get(0);
        return BeanUtil.copyProperties(payDO, PayInfoRespDTO.class);
    }

    @Override
    public PayInfoRespDTO getPayInfoByPaySn(String paySn) {
        PayDOExample example = new PayDOExample();
        example.createCriteria().andPaySnEqualTo(paySn);
        PayDO payDO = payMapper.selectByExample(example).get(0);
        return BeanUtil.copyProperties(payDO, PayInfoRespDTO.class);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long testPay(PayRequest requestParam) {
        PayDO insertPay = BeanUtil.copyProperties(requestParam, PayDO.class);
        PayDOExample payDOExample = new PayDOExample();
        payDOExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        List<PayDO> payDO = payMapper.selectByExample(payDOExample);
        if (payDO.isEmpty()){
            String paySn = PayIdGeneratorManager.generateId(requestParam.getOrderSn());
            insertPay.setId(SnowUtil.getSnowflakeNextId());
            insertPay.setPaySn(paySn);
            insertPay.setStatus(TradeStatusEnum.WAIT_BUYER_PAY.tradeCode());
            insertPay.setTotalAmount(requestParam.getTotalAmount().multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
            int insert = payMapper.insert(insertPay);
            if (insert<=0){
                log.error("支付单创建失败，支付聚合根：{}", JSON.toJSONString(requestParam));
                throw new ServiceException("支付单创建失败");
            }

        }
        // 发送支付结果回调消息
        PayCallbackReqDTO payCallbackReqDTO = BeanUtil.copyProperties(insertPay, PayCallbackReqDTO.class);
        callbackPayV2(payCallbackReqDTO);
        return Long.valueOf(UserContext.getUserId());
    }

    /**
     * 支付回调
     */
    @Transactional(rollbackFor = Exception.class)
    public void callbackPayV2(PayCallbackReqDTO requestParam) {
        PayDOExample payDOExample = new PayDOExample();
        payDOExample.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        PayDO payDO = payMapper.selectByExample(payDOExample).get(0);
        if (ObjectUtil.isEmpty(payDO)){
            log.error("支付单不存在，orderRequestId：{}", requestParam.getOrderRequestId());
            throw new ServiceException("支付单不存在");
        }

        payDO.setTradeNo(requestParam.getTradeNo());
        payDO.setStatus(TradeStatusEnum.TRADE_SUCCESS.tradeCode());
        payDO.setPayAmount(requestParam.getPayAmount());
        payDO.setGmtPayment(requestParam.getGmtPayment());
        PayDOExample example = new PayDOExample();
        example.createCriteria().andOrderSnEqualTo(requestParam.getOrderSn());
        int result = payMapper.updateByExampleSelective(payDO, example);
        if (result<=0){
            log.error("修改支付单支付结果失败，支付单信息：{}", JSON.toJSONString(payDO));
            throw new ServiceException("修改支付单支付结果失败");
        }
        // 交易成功，回调订单服务告知支付结果，修改订单流转状态
        payResultCallbackOrderSendProduce.sendMessage(BeanUtil.copyProperties(payDO, PayResultCallbackOrderEvent.class));
    }
}
