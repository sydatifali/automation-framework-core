package com.framework.listeners;

import com.framework.driver.DriverFactory;
import com.framework.utils.ConfigReader;
import com.framework.utils.ExtentReportManager;
import com.framework.utils.LoggerUtils;
import com.framework.utils.RetryUtils;
import com.framework.utils.ScreenshotUtils;
import org.apache.logging.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TestListener implements ITestListener, ISuiteListener, IAnnotationTransformer {

    private static final Logger logger = LoggerUtils.getLogger(TestListener.class);
    private static final boolean RETRY_ENABLED =
            ConfigReader.getInstance().getBoolean("retry.enabled", false);

    // ── ISuiteListener ────────────────────────────────────────────────────────

    @Override
    public void onStart(ISuite suite) {
        ExtentReportManager.initReport();
        logger.info("Suite started: {}", suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        ExtentReportManager.flushReport();
        logger.info("Suite finished: {}", suite.getName());
    }

    // ── IAnnotationTransformer ────────────────────────────────────────────────

    @SuppressWarnings("rawtypes")
    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        if (RETRY_ENABLED) {
            annotation.setRetryAnalyzer(RetryUtils.class);
        }
    }

    // ── ITestListener ─────────────────────────────────────────────────────────

    @Override
    public void onTestStart(ITestResult result) {
        int attempt = RetryUtils.getCurrentAttempt();
        String testName = attempt > 0
                ? result.getName() + " [Retry " + attempt + "]"
                : result.getName();
        ExtentReportManager.createTest(testName);
        logger.info("Test started: {}", testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentReportManager.getTest().pass("Test passed");
        logger.info("Test passed: {}", result.getName());
        cleanup();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ensureTestNodeExists(result);

        if (DriverFactory.hasDriver()) {
            String screenshotPath = ScreenshotUtils.captureScreenshot(
                    DriverFactory.getDriver(), result.getName());
            ExtentReportManager.attachScreenshot(screenshotPath);
        } else {
            logger.warn("No active WebDriver on this thread — skipping screenshot for '{}'",
                    result.getName());
        }

        Throwable cause = result.getThrowable();
        if (cause != null) {
            ExtentReportManager.getTest().fail(cause);
        } else {
            ExtentReportManager.getTest().fail("Test failed: cause not available");
        }
        logger.error("Test failed: {}", result.getName(), cause);

        if (!result.wasRetried()) {
            cleanup();
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ensureTestNodeExists(result);
        ExtentReportManager.getTest().skip("Test skipped");
        logger.warn("Test skipped: {}", result.getName());
        cleanup();
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    // Guards against @BeforeSuite / @BeforeTest / @BeforeClass failures where
    // TestNG calls onTestSkipped() or onTestFailure() without a prior onTestStart().
    private void ensureTestNodeExists(ITestResult result) {
        if (ExtentReportManager.getTest() == null) {
            logger.warn("No report node found for '{}' — creating fallback node",
                    result.getName());
            ExtentReportManager.createTest(result.getName());
        }
    }

    private void cleanup() {
        RetryUtils.resetAttempt();
        ExtentReportManager.removeTest();
    }
}
