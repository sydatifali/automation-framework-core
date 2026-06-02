package com.framework.utils;

import com.framework.exception.ConfigurationException;
import com.framework.exception.TestDataException;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryUtils implements IRetryAnalyzer {

    private static final Logger logger = LoggerUtils.getLogger(RetryUtils.class);

    private static final boolean RETRY_ENABLED =
            ConfigReader.getInstance().getBoolean("retry.enabled", false);
    private static final int MAX_RETRY_COUNT =
            ConfigReader.getInstance().getInt("retry.max.count", 2);

    private static final ThreadLocal<Integer> currentAttempt = ThreadLocal.withInitial(() -> 0);

    private int attemptCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (!RETRY_ENABLED) {
            return false;
        }

        Throwable cause = result.getThrowable();

        if (cause instanceof AssertionError) {
            logger.warn("Test '{}' failed with AssertionError — not retrying (genuine defect): {}",
                    result.getName(), cause.getMessage());
            return false;
        }

        if (cause instanceof ConfigurationException) {
            logger.error("Test '{}' failed with ConfigurationException — not retrying (unfixable): {}",
                    result.getName(), cause.getMessage());
            return false;
        }

        if (cause instanceof TestDataException) {
            logger.error("Test '{}' failed with TestDataException — not retrying (unfixable): {}",
                    result.getName(), cause.getMessage());
            return false;
        }

        if (attemptCount < MAX_RETRY_COUNT) {
            attemptCount++;
            currentAttempt.set(attemptCount);
            logger.warn("Retrying test '{}' — attempt {} of {} | cause: {} — {}",
                    result.getName(),
                    attemptCount,
                    MAX_RETRY_COUNT,
                    cause != null ? cause.getClass().getSimpleName() : "unknown",
                    cause != null ? cause.getMessage() : "");
            return true;
        }

        logger.error("Test '{}' exhausted all {} retry attempt(s) — marking as failed",
                result.getName(), MAX_RETRY_COUNT);
        return false;
    }

    public static int getCurrentAttempt() {
        return currentAttempt.get();
    }

    public static void resetAttempt() {
        currentAttempt.set(0);
    }
}
