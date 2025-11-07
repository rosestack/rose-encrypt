package io.github.rosestack.encrypt.exception;

/**
 * 加密相关异常
 *
 * @author chensoul
 * @since 1.0.0
 */
public class EncryptException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EncryptException(String message) {
        super(message);
    }

    public EncryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncryptException(Throwable cause) {
        super(cause);
    }
}
