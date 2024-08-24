package itlsy.Controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import itlsy.exception.BusinessExceptionEnum;
import itlsy.req.ConfirmOrderDoReq;
import itlsy.resp.CommonResp;
import itlsy.service.BeforeConfirmOrderService;
import itlsy.service.ConfirmOrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business/confirm-order")
public class ConfirmOrderController {
    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);
    @Autowired
    private ConfirmOrderService confirmOrderService;
    @Autowired
    private BeforeConfirmOrderService beforeConfirmOrderService;


    //接口的资源名称不要与接口路径一致，会导致先留后走不到降级方法中
    @SentinelResource(value = "confirmOrderDo",blockHandler ="interfaceBlockHandler"  )//blockHandler表示被限流或系统保护拦截后的处理方式
    @PostMapping("/do")
    public CommonResp<String> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
//        confirmOrderService.doConfirm(req);
        Long orderId = beforeConfirmOrderService.beforeDoConfirm(req);
        String id = StrUtil.toString(orderId);//传到前端时为了避免精度丢失
        return new CommonResp<>(id);
    }

    @GetMapping("/query-line-count/{id}")
    public CommonResp<Integer> queryLineCount(@PathVariable Long id){
        Integer count = confirmOrderService.queryLineCount(id);
        return new CommonResp<>(count);

    }

    @GetMapping("/cancel/{id}")
    public CommonResp<Integer> cancel(@PathVariable Long id){
        Integer cancelCount = confirmOrderService.cancel(id);
        return new CommonResp<>(cancelCount);
    }


    /**
     * 购票请求限流或降级处理逻辑(参数(要加上BlockException ex)和返回值保持一致)
     * @param req
     * @param ex
     */
    public CommonResp<Object> interfaceBlockHandler(ConfirmOrderDoReq req, BlockException ex){
        LOG.warn("接口的购票请求被限流或降级了!!!:{}",req);
        CommonResp<Object> commonResp = new CommonResp<>();
        commonResp.setSuccess(false);
        commonResp.setMessage(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION.getDesc());
        return commonResp;
    }
}
