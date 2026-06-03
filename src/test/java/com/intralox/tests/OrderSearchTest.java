package com.intralox.tests;

import com.framework.base.BaseTest;
import com.framework.utils.CredentialManager;
import com.framework.utils.ExtentReportManager;
import com.framework.utils.TestDataProvider;
import com.intralox.pages.DashboardPage;
import com.intralox.pages.LoginPage;
import com.intralox.pages.OrdersPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class OrderSearchTest extends BaseTest {

    @DataProvider(name = "orderSearchScenarios")
    public Object[][] orderSearchScenarios() {
        return TestDataProvider.fromCsv("testdata/order_search_scenarios.csv");
    }

    @Test(dataProvider = "orderSearchScenarios")
    public void searchForOrder(Map<String, String> data) {
        String scenarioName = data.get("scenarioName");
        String orderNumber  = data.get("orderNumber");
        String email        = CredentialManager.get(data.get("emailKey"));
        String password     = CredentialManager.get(data.get("passwordKey"));

        ExtentReportManager.getTest().info("Scenario: " + scenarioName);
        ExtentReportManager.getTest().info("Searching for order: " + orderNumber);

        // Step 1: Login
        LoginPage loginPage = new LoginPage(getDriver());
        DashboardPage dashboardPage = loginPage.open().loginAs(email, password);

        Assert.assertTrue(dashboardPage.isDashboardDisplayed(),
                "Dashboard should be displayed after login");
        ExtentReportManager.getTest().pass("Dashboard loaded successfully");

        // Step 2: Navigate to Orders
        OrdersPage ordersPage = dashboardPage.navigateToOrders();

        Assert.assertTrue(ordersPage.isOrdersPageDisplayed(),
                "Orders page should be displayed");
        ExtentReportManager.getTest().pass("Orders page loaded");

        // Step 3: Clear any persisted filters, then search
        ordersPage.clearFilters();
        ordersPage.searchOrder(orderNumber);

        // Step 4: Primary assertion — order link is visible in results
        Assert.assertTrue(ordersPage.isOrderVisible(orderNumber),
                "Order " + orderNumber + " should be visible in search results");
        ExtentReportManager.getTest().pass("Order " + orderNumber + " found in results");

        // Step 5: Secondary assertion — at least one result row present
        int resultCount = ordersPage.getResultCount();
        Assert.assertTrue(resultCount > 0,
                "Result table should have at least one row");
        ExtentReportManager.getTest().info("Result row count: " + resultCount);
    }
}
