package com.framework.validation;

import com.framework.base.BaseTest;
import com.framework.utils.ExtentReportManager;
import com.framework.utils.ScreenshotUtils;
import com.framework.utils.WaitUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Validates that the framework infrastructure compiles and executes end-to-end.
 *
 * Exercises:
 *   - DriverFactory: browser launch and quit (via BaseTest)
 *   - ConfigReader: property resolution (via WaitUtils constructor)
 *   - WaitUtils: waitForPageLoad()
 *   - ScreenshotUtils: captureScreenshot()
 *   - ExtentReportManager: test node creation, info logging, screenshot attachment
 *   - TestListener: onTestStart, onTestSuccess, report flush (via @Listeners on BaseTest)
 *
 * This test does not use page objects, business logic, or application-specific locators.
 */
public class FrameworkValidationTest extends BaseTest {

    @Test
    public void validateFrameworkInfrastructure() {
        // Step 1: Browser launched by BaseTest.setUp() before this method runs.
        ExtentReportManager.getTest().info("Browser launched via DriverFactory");

        // Step 2: Navigate to a known, publicly available, credential-free page.
        getDriver().get("https://example.com");

        // Validates WaitUtils.waitForPageLoad() and ConfigReader via timeout property resolution.
        WaitUtils waitUtils = new WaitUtils(getDriver());
        waitUtils.waitForPageLoad();
        ExtentReportManager.getTest().info("Navigated to https://example.com — page load confirmed");

        // Step 3: Verify page title.
        String title = getDriver().getTitle();
        ExtentReportManager.getTest().info("Page title: " + title);
        Assert.assertTrue(
                title.contains("Example Domain"),
                "Expected title to contain 'Example Domain' but was: '" + title + "'");
        ExtentReportManager.getTest().info("Title assertion passed");

        // Step 4: Capture screenshot and attach to the Extent Report.
        String screenshotPath = ScreenshotUtils.captureScreenshot(getDriver(), "validateFrameworkInfrastructure");
        ExtentReportManager.attachScreenshot(screenshotPath);
        ExtentReportManager.getTest().info("Screenshot captured: " + screenshotPath);

        // Step 5: Extent Report node is passed and flushed by TestListener.onTestSuccess()
        //         and ISuiteListener.onFinish() — no explicit call needed here.

        // Step 6: Browser closed by BaseTest.tearDown() after this method returns.
    }
}
