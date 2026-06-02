package com.intralox.tests;

import com.framework.base.BaseTest;
import com.framework.utils.CredentialManager;
import com.intralox.pages.DashboardPage;
import com.intralox.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginSmokeTest extends BaseTest {

    @Test
    public void loginAndVerifyDashboard() {
        String email    = CredentialManager.get("MYINTRALOX_EMAIL");
        String password = CredentialManager.get("MYINTRALOX_PASSWORD");

        LoginPage loginPage = new LoginPage(getDriver());
        DashboardPage dashboardPage = loginPage.open().loginAs(email, password);

        Assert.assertTrue(dashboardPage.isDashboardDisplayed(),
                "Dashboard should be displayed after successful login");
    }
}
