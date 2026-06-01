package com.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LoggerUtils {

    private LoggerUtils() {}

    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }

    public static Logger getLogger(String name) {
        return LogManager.getLogger(name);
    }
}
