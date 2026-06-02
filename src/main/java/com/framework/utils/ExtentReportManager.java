package com.framework.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.Logger;

public final class ExtentReportManager {

    private static final Logger logger = LoggerUtils.getLogger(ExtentReportManager.class);

    private static final String REPORT_PATH_KEY     = "report.output.path";
    private static final String DEFAULT_REPORT_PATH = "target/reports/extent-report.html";

    private static volatile ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    private ExtentReportManager() {}

    public static synchronized void initReport() {
        if (extentReports != null) {
            logger.warn("ExtentReportManager.initReport() called more than once — ignoring duplicate call");
            return;
        }

        String reportPath = ConfigReader.getInstance().get(REPORT_PATH_KEY, DEFAULT_REPORT_PATH);

        int lastSeparator = Math.max(reportPath.lastIndexOf('/'), reportPath.lastIndexOf('\\'));
        if (lastSeparator > 0) {
            FileUtils.ensureDirectoryExists(reportPath.substring(0, lastSeparator));
        }

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("Test Execution Report");
        spark.config().setReportName("Selenium Framework Core");
        spark.config().setEncoding("UTF-8");

        extentReports = new ExtentReports();
        extentReports.attachReporter(spark);
        extentReports.setSystemInfo("Environment",
                ConfigReader.getInstance().get("environment", "qa"));
        extentReports.setSystemInfo("Browser",
                ConfigReader.getInstance().get("browser", "chrome"));
        extentReports.setSystemInfo("Execution Mode",
                ConfigReader.getInstance().get("execution.mode", "local"));

        logger.info("Extent report initialised: {}", reportPath);
    }

    public static synchronized void flushReport() {
        if (extentReports != null) {
            extentReports.flush();
            logger.info("Extent report flushed");
        }
    }

    public static void createTest(String testName) {
        if (extentReports == null) {
            throw new IllegalStateException(
                    "ExtentReportManager has not been initialised. " +
                    "Ensure initReport() is called from ISuiteListener.onStart() before any test runs.");
        }
        extentTest.set(extentReports.createTest(testName));
    }

    public static ExtentTest getTest() {
        return extentTest.get();
    }

    public static void removeTest() {
        extentTest.remove();
    }

    public static void attachScreenshot(String screenshotPath) {
        if (screenshotPath == null || screenshotPath.isBlank()) {
            logger.warn("Screenshot path is blank — skipping attachment");
            return;
        }
        try {
            getTest().addScreenCaptureFromPath(screenshotPath);
            logger.debug("Screenshot attached to report: {}", screenshotPath);
        } catch (Exception e) {
            logger.warn("Failed to attach screenshot '{}': {}", screenshotPath, e.getMessage());
        }
    }
}
