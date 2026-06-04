package com.intralox.pages;

import com.framework.pages.BasePage;
import com.framework.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OrderDetailsPage extends BasePage {

    private static final Logger logger = LoggerUtils.getLogger(OrderDetailsPage.class);

    // ── Order header fields ───────────────────────────────────────────────────

    private static final By ORDER_NUMBER  = By.cssSelector("TODO: order number element");
    private static final By PO_NUMBER     = By.cssSelector("TODO: po number element");
    private static final By ORDER_STATUS  = By.cssSelector("TODO: order status element");
    private static final By ORDER_DATE    = By.cssSelector("TODO: order date element");
    private static final By TOTAL_AMOUNT  = By.cssSelector("TODO: total amount element");

    // ── Constructor ───────────────────────────────────────────────────────────

    public OrderDetailsPage(WebDriver driver) {
        super(driver);
    }

    // ── Load synchronization ──────────────────────────────────────────────────

    public OrderDetailsPage waitForLoad() {
        logger.info("Waiting for Order Details page to load");
        waitUtils.waitForUrlContains("/orders-management/orders/");
        return this;
    }

    // ── State queries ─────────────────────────────────────────────────────────

    public boolean isOrderDetailsDisplayed() {
        return driver.getCurrentUrl().contains("/orders-management/orders/");
    }

    // ── Field accessors ───────────────────────────────────────────────────────

    public String getOrderNumber() {
        return elementUtils.getText(ORDER_NUMBER);
    }

    public String getPoNumber() {
        return elementUtils.getText(PO_NUMBER);
    }

    public String getOrderStatus() {
        return elementUtils.getText(ORDER_STATUS);
    }

    public String getOrderDate() {
        return elementUtils.getText(ORDER_DATE);
    }

    public String getTotalAmount() {
        return elementUtils.getText(TOTAL_AMOUNT);
    }
}
