package itlsy.core;

/**
 * 幂等执行处理器
 */
public interface IdempotentExecuteHandler {

    void handler(IdempotentParamWrapper wrapper);
}
