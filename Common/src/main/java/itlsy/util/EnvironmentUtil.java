package itlsy.util;

import itlsy.ApplicationContextHolder;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * 环境工具类
 */
public class EnvironmentUtil {
    private static List<String> ENVIRONMENT_LIST=new ArrayList<>();
    static {
        ENVIRONMENT_LIST.add("dev");
        ENVIRONMENT_LIST.add("test");
    }

    /**
     * 判断当前是否为开发环境
     * @return
     */
    public static boolean isDevEnvironment(){
        ConfigurableEnvironment configurableEnvironment = ApplicationContextHolder.getBean(ConfigurableEnvironment.class);
        String propertyActive = configurableEnvironment.getProperty("spring.profiles.active", "dev");
        return ENVIRONMENT_LIST.stream().filter(item->propertyActive.contains(item)).findFirst().isPresent();
    }

    /**
     * 判断当前是否为正式环境
     * @return
     */
    public static boolean isProdEnvironment(){
        ConfigurableEnvironment configurableEnvironment = ApplicationContextHolder.getBean(ConfigurableEnvironment.class);
        String propertyActive = configurableEnvironment.getProperty("spring.profiles.active", "prod");
        return !ENVIRONMENT_LIST.stream().filter(item->propertyActive.contains(item)).findFirst().isPresent();

    }
}
