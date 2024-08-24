package itlsy.config;

import cn.hippo4j.common.executor.support.BlockingQueueTypeEnum;
import cn.hippo4j.core.executor.DynamicThreadPool;
import cn.hippo4j.core.executor.support.ThreadPoolBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Hippo4j 动态线程池配置(也可以通过配置文件的方式配置)
 */
@Configuration
public class Hippo4jThreadPoolConfiguration {

    @Bean
    @DynamicThreadPool//动态创建线程池的功能
    public ThreadPoolExecutor selectSeatThreadPoolExecutor(){
        String threadPoolId="select-seat-thread-pool-executor";
        return ThreadPoolBuilder.builder()
                .threadPoolId(threadPoolId)
                .threadFactory(threadPoolId)
                .workQueue(BlockingQueueTypeEnum.SYNCHRONOUS_QUEUE)
                .corePoolSize(10)
                .maximumPoolSize(20)
                .allowCoreThreadTimeOut(true)
                .keepAliveTime(60, TimeUnit.MINUTES)
                .dynamicPool()
                .build();
    }
}
