package com.intralox.pages;

import com.framework.pages.BasePage;
import com.framework.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OrdersPage extends BasePage {

    private static final Logger logger = LoggerUtils.getLogger(OrdersPage.class);

    // ── Page identity ─────────────────────────────────────────────────────────

    private static final By ORDERS_PAGE_INDICATOR = By.cssSelector("TODO: stable element unique to orders tab");

    // ── Search ────────────────────────────────────────────────────────────────

    private static final By ORDER_SEARCH_INPUT    = By.cssSelector("TODO: order number search input");
    private static final By SEARCH_BUTTON         = By.cssSelector("TODO: search submit button");

    // ── Results ───────────────────────────────────────────────────────────────

    // If no loading indicator exists, replace waitForInvisibility(LOADING_INDICATOR)
    // in searchOrder() with the stale-element strategy against RESULTS_CONTAINER.
    private static final By LOADING_INDICATOR     = By.cssSelector("TODO: loading spinner or overlay during search");
    private static final By RESULTS_CONTAINER     = By.cssSelector("TODO: results table or container");
    private static final By RESULT_ROWS           = By.cssSelector("TODO: individual result row selector");

    // ── Constructor ───────────────────────────────────────────────────────────

    public OrdersPage(WebDriver driver) {
        super(driver);
    }

    // ── Load synchronization ──────────────────────────────────────────────────

    public OrdersPage waitForLoad() {
        logger.info("Waiting for Orders page to load");
        waitUtils.waitForVisibility(ORDERS_PAGE_INDICATOR);
        return this;
    }

    // ── State queries ─────────────────────────────────────────────────────────

    public boolean isOrdersPageDisplayed() {
        return elementUtils.isDisplayed(ORDERS_PAGE_INDICATOR);
    }

    public boolean isOrderVisible(String orderNumber) {
        return elementUtils.isDisplayed(orderRowLocator(orderNumber));
    }

    public int getResultCount() {
        return elementUtils.count(RESULT_ROWS);
    }

    // ── Search ────────────────────────────────────────────────────────────────

    public OrdersPage searchOrder(String orderNumber) {
        logger.info("Searching for order: {}", orderNumber);
        elementUtils.clear(ORDER_SEARCH_INPUT);
        elementUtils.enterText(ORDER_SEARCH_INPUT, orderNumber);
        elementUtils.click(SEARCH_BUTTON);
        waitUtils.waitForInvisibility(LOADING_INDICATOR);
        return this;
    }

    // ── Order navigation ──────────────────────────────────────────────────────

    public OrderDetailsPage openOrder(String orderNumber) {
        logger.info("Opening order: {}", orderNumber);
        elementUtils.click(orderRowLocator(orderNumber));
        OrderDetailsPage orderDetailsPage = new OrderDetailsPage(driver);
        return orderDetailsPage.waitForLoad();
    }

    // ── Parameterized locators ────────────────────────────────────────────────

    // Constructs a row locator at call time — not stored as state.
    // XPath targets a <tr> containing a <td> whose text includes the order number.
    // Refine to the actual table structure during locator fill-in.
    private static By orderRowLocator(String orderNumber) {
        return By.xpath("//tr[.//td[contains(text(),'" + orderNumber + "')]]");
    }
}
