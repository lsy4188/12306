package itlsy.exception;

import itlsy.errorcode.BaseErrorCode;
import itlsy.errorcode.IErrorCode;

/**
 * 客户端异常
 */
public class ClientException extends AbstractException{
    public ClientException(IErrorCode errorCode){
        this(null,null,errorCode);
    }
    public ClientException(String message){
        this(message,null, BaseErrorCode.CLIENT_ERROR);
    }
    public ClientException(IErrorCode errorCode,String message){
        this(message,null,errorCode);
    }

    public ClientException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return "ClientException{" +
                "code='" + errorCode + "'," +
                "message='" + errorMessage + "'" +
                '}';
    }
}
