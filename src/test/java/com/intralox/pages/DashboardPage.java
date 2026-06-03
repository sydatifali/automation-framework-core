package com.intralox.pages;

import com.framework.pages.BasePage;
import com.framework.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {

    private static final Logger logger = LoggerUtils.getLogger(DashboardPage.class);

    // ── Page identity ─────────────────────────────────────────────────────────

    // section.user-info is structurally stable and absent on the login page.
    // Avoids dependency on user-specific text values.
    private static final By PAGE_INDICATOR    = By.cssSelector("section.user-info");

    // ── User context ──────────────────────────────────────────────────────────

    private static final By USER_DISPLAY_NAME = By.cssSelector("TODO: user display name element");

    // ── Navigation ────────────────────────────────────────────────────────────

    private static final By NAVIGATION_MENU        = By.cssSelector("TODO: navigation menu container");
    private static final By ORDER_MANAGEMENT_NAV_LINK = By.cssSelector("TODO: order management navigation link");
    private static final By ORDERS_NAV_LINK        = By.cssSelector("a[href='/en/orders-management/orders']");

    // ── Session ───────────────────────────────────────────────────────────────

    private static final By LOGOUT_CONTROL    = By.cssSelector("TODO: logout button or link");

    // ── Constructor ───────────────────────────────────────────────────────────

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    // ── Load synchronization ──────────────────────────────────────────────────

    public DashboardPage waitForLoad() {
        logger.info("Waiting for Dashboard to load");
        waitUtils.waitForVisibility(PAGE_INDICATOR);
        return this;
    }

    // ── User context ──────────────────────────────────────────────────────────

    public String getUserDisplayName() {
        logger.info("Getting user display name");
        return elementUtils.getText(USER_DISPLAY_NAME);
    }

    // ── State queries ─────────────────────────────────────────────────────────

    public boolean isDashboardDisplayed() {
        return elementUtils.isDisplayed(PAGE_INDICATOR);
    }

    public boolean isNavigationMenuDisplayed() {
        return elementUtils.isDisplayed(NAVIGATION_MENU);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    public OrderManagementPage navigateToOrderManagement() {
        logger.info("Navigating to Order Management");
        elementUtils.click(ORDER_MANAGEMENT_NAV_LINK);
        OrderManagementPage orderManagementPage = new OrderManagementPage(driver);
        return orderManagementPage.waitForLoad();
    }

    public OrdersPage navigateToOrders() {
        logger.info("Navigating to Orders");
        elementUtils.click(ORDERS_NAV_LINK);
        return new OrdersPage(driver).waitForLoad();
    }

    // ── Session management ────────────────────────────────────────────────────

    public LoginPage logout() {
        logger.info("Logging out");
        elementUtils.click(LOGOUT_CONTROL);
        LoginPage loginPage = new LoginPage(driver);
        return loginPage.waitForLoad();
    }
}
