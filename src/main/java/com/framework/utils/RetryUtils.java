package com.framework.utils;

import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryUtils implements IRetryAnalyzer {

    private static final Logger logger = LoggerUtils.getLogger(RetryUtils.class);

    private static final boolean RETRY_ENABLED =
            ConfigReader.getInstance().getBoolean("retry.enabled", false);
    private static final int MAX_RETRY_COUNT =
            ConfigReader.getInstance().getInt("retry.max.count", 2);

    // Tracks the current attempt number for the running thread so TestListener
    // can label report entries as [Retry N] without coupling to the instance.
    public static final ThreadLocal<Integer> currentAttempt = ThreadLocal.withInitial(() -> 0);

    private int attemptCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (!RETRY_ENABLED) {
            return false;
        }

        Throwable cause = result.getThrowable();
        if (cause instanceof AssertionError) {
            logger.warn("Test '{}' failed with AssertionError — not retrying (genuine defect)",
                    result.getName());
            return false;
        }

        if (attemptCount < MAX_RETRY_COUNT) {
            attemptCount++;
            currentAttempt.set(attemptCount);
            logger.warn("Retrying test '{}' — attempt {} of {} | cause: {}",
                    result.getName(), attemptCount, MAX_RETRY_COUNT,
                    cause != null ? cause.getClass().getSimpleName() : "unknown");
            return true;
        }

        logger.error("Test '{}' exhausted all {} retry attempts", result.getName(), MAX_RETRY_COUNT);
        return false;
    }

    // Called by TestListener after each test method completes (pass or final fail)
    // to reset the thread-local so the next test starts at attempt 0.
    public static void resetAttempt() {
        currentAttempt.set(0);
    }
}
