package itlsy.req;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分页请求对象
 * <p> {@link PageReq}、{@link PageResponse}
 *  * 可以理解是防腐层的一种实现，不论底层 ORM 框架，对外分页参数属性不变
 */
@Data
public class PageReq {

//    @NotNull(message = "【页码】不能为空")
//    private Integer page;

//    @NotNull(message = "【每页条数】不能为空")
//    @Max(value = 100, message = "【每页条数】不能超过100")
//    private Integer size;

    /**
     * 当前页
     */
    private Integer page = 1;

    /**
     * 每页显示条数
     */
    private Integer size = 10;


}
