package com.intralox.pages;

import com.framework.pages.BasePage;
import com.framework.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class OrdersPage extends BasePage {

    private static final Logger logger = LoggerUtils.getLogger(OrdersPage.class);

    // ── Search ────────────────────────────────────────────────────────────────

    private static final By ORDER_SEARCH_INPUT = By.cssSelector("input[type='search']");

    // ── Filters ───────────────────────────────────────────────────────────────

    private static final By CLEAR_ALL_BUTTON = By.xpath("//*[normalize-space(text())='Clear All']");

    // ── Results ───────────────────────────────────────────────────────────────

    // tr[role='row'] targets ARIA table rows. May include the header row —
    // verify during test execution and adjust getResultCount() if needed.
    private static final By RESULT_ROWS = By.cssSelector("tr[role='row']");

    // ── Constructor ───────────────────────────────────────────────────────────

    public OrdersPage(WebDriver driver) {
        super(driver);
    }

    // ── Load synchronization ──────────────────────────────────────────────────

    public OrdersPage waitForLoad() {
        logger.info("Waiting for Orders page to load");
        waitUtils.waitForUrlContains("/orders-management/orders");
        return this;
    }

    // ── State queries ─────────────────────────────────────────────────────────

    public boolean isOrdersPageDisplayed() {
        return driver.getCurrentUrl().contains("/orders-management/orders");
    }

    public boolean isOrderVisible(String orderNumber) {
        try {
            waitUtils.waitForVisibility(By.linkText(orderNumber));
            return true;
        } catch (Exception e) {
            logger.debug("Order link not visible within timeout: {}", orderNumber);
            return false;
        }
    }

    public int getResultCount() {
        return elementUtils.count(RESULT_ROWS);
    }

    // ── Search ────────────────────────────────────────────────────────────────

    public OrdersPage clearFilters() {
        logger.info("Clearing all active filters");
        if (elementUtils.isPresent(CLEAR_ALL_BUTTON)) {
            elementUtils.click(CLEAR_ALL_BUTTON);
            logger.info("Active filters cleared");
        } else {
            logger.info("No active filters present");
        }
        return this;
    }

    public OrdersPage searchOrder(String orderNumber) {
        logger.info("Searching for order: {}", orderNumber);
        elementUtils.enterText(ORDER_SEARCH_INPUT, orderNumber);
        waitUtils.waitForVisibility(ORDER_SEARCH_INPUT).sendKeys(Keys.RETURN);
        return this;
    }

    // ── Order navigation ──────────────────────────────────────────────────────

    public OrderDetailsPage openOrder(String orderNumber) {
        logger.info("Opening order: {}", orderNumber);
        elementUtils.click(By.linkText(orderNumber));
        return new OrderDetailsPage(driver).waitForLoad();
    }
}
