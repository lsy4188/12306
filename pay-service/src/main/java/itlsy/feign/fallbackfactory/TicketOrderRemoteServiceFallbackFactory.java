package itlsy.feign.fallbackfactory;

import itlsy.feign.TicketOrderRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TicketOrderRemoteServiceFallbackFactory implements FallbackFactory<TicketOrderRemoteService> {

    @Override
    public TicketOrderRemoteService create(Throwable cause) {
        return item->{
            log.error("调用票务服务失败",cause);
            return null;
        };
    }
}
