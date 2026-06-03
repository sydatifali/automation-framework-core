package com.intralox.pages;

import com.framework.pages.BasePage;
import com.framework.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class OrderManagementPage extends BasePage {

    private static final Logger logger = LoggerUtils.getLogger(OrderManagementPage.class);

    // ── Tabs ──────────────────────────────────────────────────────────────────

    private static final By ORDERS_TAB = By.cssSelector("TODO: orders tab button");

    // ── Constructor ───────────────────────────────────────────────────────────

    public OrderManagementPage(WebDriver driver) {
        super(driver);
    }

    // ── Load synchronization ──────────────────────────────────────────────────

    public OrderManagementPage waitForLoad() {
        logger.info("Waiting for Order Management page to load");
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.urlContains("/orders-management"));
        return this;
    }

    // ── State queries ─────────────────────────────────────────────────────────

    public boolean isOrderManagementDisplayed() {
        return driver.getCurrentUrl().contains("/orders-management");
    }

    // ── Tab navigation ────────────────────────────────────────────────────────

    public OrdersPage selectOrdersTab() {
        logger.info("Selecting Orders tab");
        elementUtils.click(ORDERS_TAB);
        OrdersPage ordersPage = new OrdersPage(driver);
        return ordersPage.waitForLoad();
    }

    // Future tabs — selectInvoicesTab(), selectShipmentsTab(), selectQuotesTab()
    // will be added here when those workflows are in scope.
}
