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

public class NavigationSmokeTest extends BaseTest {

    @DataProvider(name = "navigationScenarios")
    public Object[][] navigationScenarios() {
        return TestDataProvider.fromCsv("testdata/navigation_scenarios.csv");
    }

    @Test(dataProvider = "navigationScenarios")
    public void navigateToOrders(Map<String, String> data) {
        String scenarioName = data.get("scenarioName");
        String expectedPage = data.get("expectedPage");
        String email        = CredentialManager.get(data.get("emailKey"));
        String password     = CredentialManager.get(data.get("passwordKey"));

        ExtentReportManager.getTest().info("Scenario: " + scenarioName);
        ExtentReportManager.getTest().info("Target page: " + expectedPage);

        // Step 1: Login
        LoginPage loginPage = new LoginPage(getDriver());
        DashboardPage dashboardPage = loginPage.open().loginAs(email, password);

        Assert.assertTrue(dashboardPage.isDashboardDisplayed(),
                "Dashboard should be displayed after login");
        ExtentReportManager.getTest().pass("Dashboard loaded successfully");

        // Step 2: Navigate to Orders
        OrdersPage ordersPage = dashboardPage.navigateToOrders();

        Assert.assertTrue(ordersPage.isOrdersPageDisplayed(),
                "Orders page should be displayed. Expected page: " + expectedPage);
        ExtentReportManager.getTest().pass("Navigated to: " + expectedPage);
    }
}
