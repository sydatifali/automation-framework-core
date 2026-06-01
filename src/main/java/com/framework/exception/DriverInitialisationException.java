package com.framework.exception;

public class DriverInitialisationException extends FrameworkException {

    public DriverInitialisationException(String message) {
        super(message);
    }

    public DriverInitialisationException(String message, Throwable cause) {
        super(message, cause);
    }
}
