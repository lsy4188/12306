package itlsy.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor//所有参数构造函数
public class BusinessException extends RuntimeException {

    private BusinessExceptionEnum e;

    /**
     * 不写入堆栈信息，提高性能
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
