package itlsy.feign;

import itlsy.feign.fallback.NumberFallBackFactory;
import itlsy.req.NumberTicketReq;
import itlsy.resp.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(value = "Number",url="http://localhost:8080")

@FeignClient(contextId = "NumberFeign",value = "Number",fallbackFactory = NumberFallBackFactory.class)
public interface NumberFeign {
    @GetMapping("/number/feign/ticket/save")
    CommonResp<Object> save(@RequestBody NumberTicketReq req);
}
