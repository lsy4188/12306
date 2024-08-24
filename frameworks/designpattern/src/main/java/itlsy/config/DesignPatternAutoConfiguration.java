package itlsy.config;


import itlsy.chain.AbstractChainContext;
import itlsy.strategy.AbstractStrategyChoose;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 设计模式自动装配
 */
@ImportAutoConfiguration(ApplicationBaseAutoConfiguration.class)
public class DesignPatternAutoConfiguration {
    /**
     * 策略模式选择器
     */
    @Bean
    public AbstractStrategyChoose abstractStrategyChoose(){
        return new AbstractStrategyChoose();
    }
    @Bean
    public AbstractChainContext abstractChainContext(){
        return new AbstractChainContext();
    }

}
