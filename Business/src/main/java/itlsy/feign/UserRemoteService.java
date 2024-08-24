package itlsy.feign;

import itlsy.feign.dto.PassengerRespDTO;
import itlsy.feign.fallback.UserRemoteFallbackFactory;
import itlsy.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户远程服务调用(当远程调用同一个模块时需要指定contextId)
 */
@FeignClient( contextId = "UserRemoteService",value = "Number",fallbackFactory = UserRemoteFallbackFactory.class )
public interface UserRemoteService {

    @GetMapping("/api/number/passenger/query/ids")
    public Result<List<PassengerRespDTO>> listPassengerQueryByIds(@RequestParam("username") String username, @RequestParam("ids") List<String> ids);
}
