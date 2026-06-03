package com.intralox.pages;

import com.framework.pages.BasePage;
import com.framework.utils.ConfigReader;
import com.framework.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final Logger logger = LoggerUtils.getLogger(LoginPage.class);

    // ── State 1: Email step ───────────────────────────────────────────────────

    private static final By EMAIL_INPUT    = By.name("identifier");

    // ── State 2: Password step ────────────────────────────────────────────────

    private static final By PASSWORD_INPUT = By.name("credentials.passcode");

    // ── Error state ───────────────────────────────────────────────────────────

    private static final By ERROR_MESSAGE   = By.cssSelector("TODO: error message element");

    // ── Page identity ─────────────────────────────────────────────────────────

    // Reused from EMAIL_INPUT — the email field is the most reliable indicator that the
    // login form is rendered and interactive. Absent on all other pages.
    private static final By PAGE_INDICATOR  = By.name("identifier");

    // ── Constructor ───────────────────────────────────────────────────────────

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // ── Load synchronization ──────────────────────────────────────────────────

    public LoginPage waitForLoad() {
        waitUtils.waitForVisibility(PAGE_INDICATOR);
        return this;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    public LoginPage open() {
        navigateTo(ConfigReader.getInstance().get("app.base.url"));
        waitUtils.waitForVisibility(PAGE_INDICATOR);
        return this;
    }

    // ── State 1 interactions ──────────────────────────────────────────────────

    public LoginPage enterEmail(String email) {
        logger.info("Entering email: {}", email);
        elementUtils.enterText(EMAIL_INPUT, email);
        return this;
    }

    public LoginPage continueSignIn() {
        logger.info("Submitting email step");
        waitUtils.waitForVisibility(EMAIL_INPUT).sendKeys(Keys.RETURN);
        waitUtils.waitForVisibility(PASSWORD_INPUT);
        return this;
    }

    // ── State 2 interactions ──────────────────────────────────────────────────

    public boolean isPasswordStepDisplayed() {
        return elementUtils.isDisplayed(PASSWORD_INPUT);
    }

    public LoginPage enterPassword(String password) {
        logger.info("Entering password");
        elementUtils.enterText(PASSWORD_INPUT, password);
        return this;
    }

    public LoginPage verifyLogin() {
        logger.info("Submitting password step");
        waitUtils.waitForVisibility(PASSWORD_INPUT).sendKeys(Keys.RETURN);
        return this;
    }

    // ── Orchestration: success path ───────────────────────────────────────────

    public DashboardPage loginAs(String email, String password) {
        logger.info("Logging in as: {}", email);
        enterEmail(email);
        continueSignIn();
        enterPassword(password);
        verifyLogin();
        DashboardPage dashboardPage = new DashboardPage(driver);
        return dashboardPage.waitForLoad();
    }

    // ── Orchestration: failure path ───────────────────────────────────────────

    public LoginPage attemptLoginAs(String email, String password) {
        logger.info("Attempting login (expecting failure) as: {}", email);
        enterEmail(email);
        continueSignIn();
        enterPassword(password);
        verifyLogin();
        waitUtils.waitForVisibility(ERROR_MESSAGE);
        return this;
    }

    // ── Error state ───────────────────────────────────────────────────────────

    public String getErrorMessage() {
        return elementUtils.getText(ERROR_MESSAGE);
    }

    public boolean isErrorDisplayed() {
        return elementUtils.isDisplayed(ERROR_MESSAGE);
    }

    // ── Page state query ──────────────────────────────────────────────────────

    public boolean isLoginPageDisplayed() {
        return elementUtils.isDisplayed(PAGE_INDICATOR);
    }
}
