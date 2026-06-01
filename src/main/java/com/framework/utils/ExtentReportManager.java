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

    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    private ExtentReportManager() {}

    // Called once per suite from TestListener.onStart().
    public static synchronized void initReport() {
        String reportPath = ConfigReader.getInstance().get(REPORT_PATH_KEY, DEFAULT_REPORT_PATH);
        String reportDir  = reportPath.substring(0, Math.max(reportPath.lastIndexOf('/'),
                                                              reportPath.lastIndexOf('\\')) + 1);
        if (!reportDir.isBlank()) {
            FileUtils.ensureDirectoryExists(reportDir);
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

    // Called once per suite from TestListener.onFinish().
    public static synchronized void flushReport() {
        if (extentReports != null) {
            extentReports.flush();
            logger.info("Extent report flushed");
        }
    }

    // Called at the start of each test method from TestListener.
    public static void createTest(String testName) {
        ExtentTest test = extentReports.createTest(testName);
        extentTest.set(test);
    }

    // Returns the ExtentTest node for the current thread.
    public static ExtentTest getTest() {
        return extentTest.get();
    }

    // Removes the current thread's ExtentTest node.
    // Called from TestListener after each test method completes.
    public static void removeTest() {
        extentTest.remove();
    }

    // Attaches a screenshot to the current thread's test node.
    // screenshotPath is the absolute file path returned by ScreenshotUtils.
    // A blank path (capture failed) is silently ignored — report integrity is preserved.
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
