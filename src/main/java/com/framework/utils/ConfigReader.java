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
        String envValue = System.getenv(toEnvVarKey(key));
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        String sysValue = System.getProperty(key);
        if (sysValue != null && !sysValue.isBlank()) {
            return sysValue.trim();
        }

        String propValue = properties.getProperty(key);
        if (propValue == null) {
            throw new ConfigurationException(
                    "Required configuration key not found: '" + key + "'. " +
                    "Set it in config.properties, config-{env}.properties, " +
                    "or via the " + toEnvVarKey(key) + " environment variable.");
        }
        return propValue.trim();
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.error("Invalid integer for key [{}]: '{}' — using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logger.error("Invalid long for key [{}]: '{}' — using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) return defaultValue;
        return Boolean.parseBoolean(value);
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
