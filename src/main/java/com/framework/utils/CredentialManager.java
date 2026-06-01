package com.framework.utils;

import com.framework.exception.ConfigurationException;
import org.apache.logging.log4j.Logger;

public final class CredentialManager {

    private static final Logger logger = LoggerUtils.getLogger(CredentialManager.class);

    private CredentialManager() {}

    // Returns the value of the environment variable identified by key.
    // Throws ConfigurationException if the variable is absent or blank —
    // a missing credential is a configuration error, not a recoverable condition.
    public static String get(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(
                    "Required credential '" + key + "' is not set. " +
                    "Set it as an environment variable before running the suite. " +
                    "For BlazeMeter, configure it as a secure variable in the test plan.");
        }
        logger.debug("Credential resolved: {}", key);
        return value;
    }

    // Returns the value of the environment variable, or null if absent or blank.
    // Use only for credentials that are genuinely optional in some environments.
    public static String getOptional(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            logger.debug("Optional credential not set: {}", key);
            return null;
        }
        logger.debug("Optional credential resolved: {}", key);
        return value;
    }
}
