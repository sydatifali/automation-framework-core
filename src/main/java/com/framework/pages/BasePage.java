package com.framework.pages;

import com.framework.utils.ElementUtils;
import com.framework.utils.LoggerUtils;
import com.framework.utils.WaitUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

public abstract class BasePage {

    private static final Logger logger = LoggerUtils.getLogger(BasePage.class);

    protected final WebDriver driver;
    protected final WaitUtils waitUtils;
    protected final ElementUtils elementUtils;

    protected BasePage(WebDriver driver) {
        this.driver       = driver;
        this.waitUtils    = new WaitUtils(driver);
        this.elementUtils = new ElementUtils(driver, waitUtils);
    }

    // Opens the given URL and waits for the page to reach document.readyState === 'complete'.
    // For pages with significant AJAX-driven content, follow this with an element-level wait
    // in the consumer page class.
    protected void navigateTo(String url) {
        logger.info("Navigating to: {}", url);
        driver.get(url);
        waitUtils.waitForPageLoad();
    }
}
