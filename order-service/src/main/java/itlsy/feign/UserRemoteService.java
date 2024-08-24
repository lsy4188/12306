package itlsy.feign;


import itlsy.feign.dto.UserQueryActualRespDTO;
import itlsy.feign.fallbackFactory.UserRemoteServiceFactory;
import itlsy.result.Result;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户远程服务调用
 */
@FeignClient(value = "Number",fallbackFactory = UserRemoteServiceFactory.class )
public interface UserRemoteService {
    /**
     * 根据乘车人 ID 集合查询乘车人列表
     */
    @GetMapping("/api/number/actual/query")
    public Result<UserQueryActualRespDTO> queryActualUserByUsername(@RequestParam("username") @NotEmpty String username);
}
