package com.framework.utils;

import com.framework.exception.ConfigurationException;
import org.apache.logging.log4j.Logger;

public final class CredentialManager {

    private static final Logger logger = LoggerUtils.getLogger(CredentialManager.class);

    private CredentialManager() {}

    public static String get(String key) {
        if (key == null || key.isBlank()) {
            throw new ConfigurationException(
                    "Credential key must not be null or blank.");
        }
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(
                    "Required credential '" + key + "' is not set. " +
                    "Set it as an environment variable before running the suite. " +
                    "For BlazeMeter, configure it as a secure variable in the test plan.");
        }
        logger.debug("Credential resolved: {}", key);
        return value.trim();
    }

    public static String getOptional(String key) {
        if (key == null || key.isBlank()) {
            throw new ConfigurationException(
                    "Credential key must not be null or blank.");
        }
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            logger.debug("Optional credential not set: {}", key);
            return null;
        }
        logger.debug("Optional credential resolved: {}", key);
        return value.trim();
    }
}
