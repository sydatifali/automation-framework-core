package com.framework.utils;

import com.framework.exception.ConfigurationException;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class CredentialManager {

    private static final Logger logger = LoggerUtils.getLogger(CredentialManager.class);

    // Loaded once at class initialisation. Empty when the file is absent — that is the normal
    // state in CI/CD and BlazeMeter, where credentials are supplied as environment variables.
    private static final Properties localSecrets = loadLocalSecrets();

    private CredentialManager() {}

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Resolves a required credential using the layered strategy:
     * 1. OS environment variable (CI/CD, BlazeMeter secure variables)
     * 2. src/test/resources/local-secrets.properties (local dev only — never committed)
     * 3. ConfigurationException
     */
    public static String get(String key) {
        validateKey(key);

        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            logger.debug("Credential '{}' resolved from environment variable", key);
            return value.trim();
        }

        value = localSecrets.getProperty(key);
        if (value != null && !value.isBlank()) {
            logger.debug("Credential '{}' resolved from local-secrets.properties", key);
            return value.trim();
        }

        throw new ConfigurationException(
                "Required credential '" + key + "' is not set. " +
                "Options: (1) set as an OS environment variable, " +
                "(2) add to src/test/resources/local-secrets.properties (local dev only — never commit this file).");
    }

    /**
     * Resolves an optional credential. Returns null when not found in any source.
     */
    public static String getOptional(String key) {
        validateKey(key);

        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            logger.debug("Optional credential '{}' resolved from environment variable", key);
            return value.trim();
        }

        value = localSecrets.getProperty(key);
        if (value != null && !value.isBlank()) {
            logger.debug("Optional credential '{}' resolved from local-secrets.properties", key);
            return value.trim();
        }

        logger.debug("Optional credential '{}' not set in any source", key);
        return null;
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private static void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new ConfigurationException("Credential key must not be null or blank.");
        }
    }

    private static Properties loadLocalSecrets() {
        Properties props = new Properties();
        try (InputStream stream = CredentialManager.class.getClassLoader()
                .getResourceAsStream("local-secrets.properties")) {
            if (stream != null) {
                props.load(stream);
                LoggerUtils.getLogger(CredentialManager.class)
                        .debug("Loaded local-secrets.properties (local dev credentials)");
            }
        } catch (IOException e) {
            LoggerUtils.getLogger(CredentialManager.class)
                    .warn("Could not load local-secrets.properties: {}", e.getMessage());
        }
        return props;
    }
}
