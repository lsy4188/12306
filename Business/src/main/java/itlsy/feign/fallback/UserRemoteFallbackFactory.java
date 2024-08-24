package itlsy.feign.fallback;

import itlsy.feign.UserRemoteService;
import itlsy.result.Result;
import itlsy.result.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserRemoteFallbackFactory implements FallbackFactory<UserRemoteService> {
    @Override
    public UserRemoteService create(Throwable cause) {
        return (userName,ids)->{
            log.error("feign调用失败，失败原因：{}",cause.getMessage());
            return Results.success(null);
        };
    }
}
