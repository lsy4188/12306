package itlsy.feign.fallback;

import itlsy.feign.NumberFeign;
import itlsy.resp.CommonResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NumberFallBackFactory implements FallbackFactory<NumberFeign> {
    @Override
    public NumberFeign create(Throwable cause) {
        return req -> {
            log.error("调用保存接口异常", cause);
            return new CommonResp<>();
        };
    }

}
