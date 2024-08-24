package itlsy.feign;


import itlsy.feign.dto.RefundReqDTO;
import itlsy.feign.dto.RefundRespDTO;
import itlsy.feign.fallback.PayRemoteServiceFallbackFactory;
import itlsy.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 支付单远程调用服务
 */
@FeignClient(value = "pay-service",fallbackFactory = PayRemoteServiceFallbackFactory.class)
public interface PayRemoteService {
    /**
     * 公共退款接口
     */
    @PostMapping("/api/pay-service/common/refund")
    Result<RefundRespDTO> commonRefund(@RequestBody RefundReqDTO requestParam);
}
