package itlsy.Controller;

import itlsy.req.RefundReqDTO;
import itlsy.resp.RefundRespDTO;
import itlsy.result.Result;
import itlsy.result.Results;
import itlsy.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 退款控制层
 */
@RestController
@RequestMapping("/api/pay-service")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @PostMapping("/common/refund")
    public Result<RefundRespDTO> commonRefund(@RequestBody RefundReqDTO requestParam) {
        return Results.success(refundService.commonRefundV2(requestParam));
    }
}
