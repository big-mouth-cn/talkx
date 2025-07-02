package com.bxm.warcar.message;

/**
 * <h3>发送消息失败异常</h3>
 *
 * @author hcmony
 * @since V1.0.0 2018/07/30
 */
public class MessageException extends RuntimeException {

    /**
     * 失败编码
     */
    private final String code;

    public MessageException(String message) {
        this(message, null);
    }

    public MessageException(String message, String code) {
        super(message);
        this.code = code;
    }

    public MessageException(String message, Throwable cause, String code) {
        super(message, cause);
        this.code = code;
    }

    public MessageException(Throwable cause, String code) {
        super(cause);
        this.code = code;
    }

    public MessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
