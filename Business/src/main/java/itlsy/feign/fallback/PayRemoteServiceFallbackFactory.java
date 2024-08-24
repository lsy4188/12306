package itlsy.feign.fallback;

import itlsy.feign.PayRemoteService;
import itlsy.feign.dto.RefundReqDTO;
import itlsy.feign.dto.RefundRespDTO;
import itlsy.result.Result;
import itlsy.result.Results;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class PayRemoteServiceFallbackFactory implements FallbackFactory<PayRemoteService> {
    @Override
    public PayRemoteService create(Throwable cause) {
        return new PayRemoteService() {
            @Override
            public Result<RefundRespDTO> commonRefund(RefundReqDTO requestParam) {
                return Results.success(null);
            }
        };
    }
}
