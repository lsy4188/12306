package itlsy.feign.fallbackFactory;

import itlsy.feign.UserRemoteService;
import itlsy.feign.dto.UserQueryActualRespDTO;
import itlsy.result.Result;
import itlsy.result.Results;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class UserRemoteServiceFactory implements FallbackFactory<UserRemoteService> {
    @Override
    public UserRemoteService create(Throwable cause) {
        return new UserRemoteService() {
            @Override
            public Result<UserQueryActualRespDTO> queryActualUserByUsername(String username) {
                return Results.success(null);
            }
        };
    }
}
