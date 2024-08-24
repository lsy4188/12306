package itlsy.service;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import itlsy.context.LoginMemberContext;
import itlsy.dto.ConfirmOrderMQDto;
import itlsy.entry.ConfirmOrder;
import itlsy.enums.ConfirmOrderStatusEnum;
import itlsy.exception.BusinessException;
import itlsy.exception.BusinessExceptionEnum;
import itlsy.mapper.ConfirmOrderMapper;
import itlsy.req.ConfirmOrderDoReq;
import itlsy.req.ConfirmOrderTicketReq;
import itlsy.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BeforeConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(BeforeConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Autowired
    private SkTokenService skTokenService;

    @Autowired
    private ConfirmOrderService confirmOrderService;


    @SentinelResource(value = "beforeDoConfirm", blockHandler = "beforeDoConfirmBlock")
    public Long beforeDoConfirm(ConfirmOrderDoReq req) {

        Long id = null;//返回最后一个用户的id
        for (int i = 0; i < req.getLineNumber() + 1; i++) {
            //获得numberId
            req.setNumberId(LoginMemberContext.getId());
            //首先在进去之前要校验令牌数量
            boolean validSkToken = skTokenService.validSkToken(req.getDate(), req.getTrainCode(), req.getNumberId());
            if (validSkToken) {
                LOG.info("校验令牌数量成功");
            } else {
                LOG.info("校验令牌数量失败");
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
            }


            List<ConfirmOrderTicketReq> tickets = req.getTickets();
            // 保存确认订单表，状态初始
            Date date = new Date();
            ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class, "tickets");
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            //confirmOrder.setNumberId(LoginMemberContext.getId());
            confirmOrder.setNumberId(req.getNumberId());
            confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
            confirmOrder.setCreateTime(date);
            confirmOrder.setUpdateTime(date);
            confirmOrder.setTickets(JSON.toJSONString(tickets));
            confirmOrderMapper.insert(confirmOrder);


            ConfirmOrderMQDto confirmOrderMQDto = new ConfirmOrderMQDto();
            confirmOrderMQDto.setDate(req.getDate());
            confirmOrderMQDto.setTrainCode(req.getTrainCode());
            confirmOrderMQDto.setLogId(MDC.get("LOG_ID"));
            confirmOrderService.doConfirm(confirmOrderMQDto);
            id = confirmOrder.getId();
        }
        return id;
    }

    /**
     * 降级方法，需包含限流方法的所有参数和BlockException参数
     *
     * @param req
     * @param e
     */
    public void beforeDoConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.info("购票请求被限流：{}", req);
        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
    }
}
