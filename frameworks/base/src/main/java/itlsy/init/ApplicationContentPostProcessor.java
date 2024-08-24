package itlsy.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor//通过构造方法注入applicationContext属性
public class ApplicationContentPostProcessor implements ApplicationListener<ApplicationReadyEvent> {

    private final ApplicationContext applicationContext;
    /**
     * 执行标识，确保Spring事件 {@link ApplicationReadyEvent} 有且执行一次
     */
    private final AtomicBoolean executeOnlyOnce=new AtomicBoolean(false);
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!executeOnlyOnce.compareAndSet(false,true)){
            return;
        }
        applicationContext.publishEvent(new ApplicationInitializingEvent(this));
    }
}
