package com.cqx.netty.sdtp.exception;

/**
 * SdtpException
 *
 * @author chenqixu
 */
public class SdtpException extends RuntimeException {
    public SdtpException() {
        super();
    }

    public SdtpException(String message) {
        super(message);
    }

    public SdtpException(String message, Throwable cause) {
        super(message, cause);
    }

    public SdtpException(Throwable cause) {
        super(cause);
    }
}
