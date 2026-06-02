package com.framework.base;

import com.framework.driver.DriverFactory;
import com.framework.listeners.TestListener;
import com.framework.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * Base class for all test classes.
 *
 * Lifecycle methods setUp() and tearDown() are final — do not override them.
 * To run code after the driver starts or before it quits, override the hook
 * methods onSetUp() and onTearDown() instead.
 *
 * Access the WebDriver via getDriver(). Do not hold a separate reference to it.
 */
@Listeners(TestListener.class)
public abstract class BaseTest {

    private static final Logger logger = LoggerUtils.getLogger(BaseTest.class);

    private WebDriver driver;

    // ── Lifecycle — final, not overridable ────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    public final void setUp() {
        driver = DriverFactory.getDriver();
        logger.info("WebDriver session started for thread: {}", Thread.currentThread().getId());
        onSetUp();
    }

    @AfterMethod(alwaysRun = true)
    public final void tearDown() {
        onTearDown();
        logger.info("Quitting WebDriver session for thread: {}", Thread.currentThread().getId());
        DriverFactory.quitDriver();
        driver = null;
    }

    // ── Extension hooks — override these, not setUp/tearDown ─────────────────

    protected void onSetUp() {}

    protected void onTearDown() {}

    // ── Driver access ─────────────────────────────────────────────────────────

    protected WebDriver getDriver() {
        return driver;
    }
}
