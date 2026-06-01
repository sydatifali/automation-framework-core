package com.framework.base;

import com.framework.driver.DriverFactory;
import com.framework.listeners.TestListener;
import com.framework.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners(TestListener.class)
public abstract class BaseTest {

    private static final Logger logger = LoggerUtils.getLogger(BaseTest.class);

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.getDriver();
        logger.info("WebDriver session started for thread: {}", Thread.currentThread().getId());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        logger.info("Quitting WebDriver session for thread: {}", Thread.currentThread().getId());
        DriverFactory.quitDriver();
        driver = null;
    }
}
