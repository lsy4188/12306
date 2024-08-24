package itlsy.Controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
public class TestController {

    //测试限流规则
    @SentinelResource("hello")
    @GetMapping("/hello")
    public String hello() throws InterruptedException {

        /*用于测试熔断降级中的-->异常比例0.3
        int random = new RandomUtil().randomInt(1, 10);
        if (random <=3){
            throw new RuntimeException("异常测试");
        }*/
        //Thread.sleep(500);配合batch中的远程调用测试熔断降级规则-->慢调用比例

        return "Hello, Business!";
    }

//      测试流控模式为关联的例子
//    @SentinelResource("hello1")
//    @GetMapping("/hello1")
//    public String hello1() throws InterruptedException {
//        Thread.sleep(500);
//        return "Hello, Business1!";
//    }
}
