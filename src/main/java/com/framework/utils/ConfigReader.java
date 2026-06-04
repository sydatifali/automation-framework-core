package com.framework.utils;

import com.framework.exception.ConfigurationException;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

public final class ConfigReader {

    private static final Logger logger = LoggerUtils.getLogger(ConfigReader.class);
    private static volatile ConfigReader instance;
    private final Properties properties;

    private static final Set<String> SUPPORTED_ENVIRONMENTS = Set.of("qa", "uat", "staging", "prod");

    private ConfigReader() {
        properties = new Properties();
        String environment = resolveEnvironment();
        loadRequired("config.properties");
        loadOptional("config-" + environment + ".properties");
    }

    public static ConfigReader getInstance() {
        if (instance == null) {
            synchronized (ConfigReader.class) {
                if (instance == null) {
                    instance = new ConfigReader();
                }
            }
        }
        return instance;
    }

    public String get(String key) {
        String value = findValue(key);
        if (value != null) {
            return value.trim();
        }
        throw new ConfigurationException(
                "Required configuration key not found: '" + key + "'. " +
                "Set it in config.properties, config-{env}.properties, " +
                "or via the " + toEnvVarKey(key) + " environment variable.");
    }

    public String get(String key, String defaultValue) {
        String value = findValue(key);
        return (value != null && !value.isBlank()) ? value.trim() : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        String value = findValue(key);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.error("Invalid integer for key [{}]: '{}' — using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        String value = findValue(key);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            logger.error("Invalid long for key [{}]: '{}' — using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = findValue(key);
        if (value == null || value.isBlank()) return defaultValue;
        return Boolean.parseBoolean(value.trim());
    }

    // Returns the resolved value for key, or null if the key is absent from all sources.
    // Env vars and system properties are trimmed and must be non-blank to match.
    // A properties-file value is returned as-is (possibly blank) — callers check isBlank().
    private String findValue(String key) {
        String envValue = System.getenv(toEnvVarKey(key));
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String sysValue = System.getProperty(key);
        if (sysValue != null && !sysValue.isBlank()) {
            return sysValue.trim();
        }
        return properties.getProperty(key);
    }

    private String resolveEnvironment() {
        String env = System.getProperty("environment");
        if (env == null || env.isBlank()) {
            env = System.getenv("ENVIRONMENT");
        }
        if (env == null || env.isBlank()) {
            env = "qa";
        }
        env = env.trim().toLowerCase();

        if (!SUPPORTED_ENVIRONMENTS.contains(env)) {
            throw new ConfigurationException(
                    "Unsupported environment: '" + env + "'. " +
                    "Supported values: " + SUPPORTED_ENVIRONMENTS + ". " +
                    "Set via -Denvironment=<value> (Maven) or ENVIRONMENT environment variable.");
        }

        logger.info("Active environment: {}", env);
        return env;
    }

    private void loadRequired(String resourcePath) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new ConfigurationException(
                        "Required configuration file not found on classpath: '" + resourcePath + "'. " +
                        "Ensure config.properties exists under src/test/resources/.");
            }
            properties.load(stream);
            logger.debug("Loaded configuration: {}", resourcePath);
        } catch (IOException e) {
            throw new ConfigurationException(
                    "Failed to load configuration: " + resourcePath, e);
        }
    }

    private void loadOptional(String resourcePath) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                logger.warn("Optional overlay not found — base config applies: {}", resourcePath);
                return;
            }
            properties.load(stream);
            logger.debug("Loaded environment overlay: {}", resourcePath);
        } catch (IOException e) {
            logger.error("Failed to load optional configuration: {}", resourcePath, e);
        }
    }

    private String toEnvVarKey(String key) {
        return key.toUpperCase().replace(".", "_").replace("-", "_");
    }
}
