package com.framework.utils;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class WaitUtils {

    private static final Logger logger = LoggerUtils.getLogger(WaitUtils.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final long defaultTimeout;

    public WaitUtils(WebDriver driver) {
        this.driver         = driver;
        this.defaultTimeout = ConfigReader.getInstance().getLong("wait.explicit.timeout", 15);
        this.wait           = new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout));
        logger.debug("WaitUtils initialised with default timeout: {}s", defaultTimeout);
    }

    // ── Visibility ────────────────────────────────────────────────────────────

    public WebElement waitForVisibility(By locator) {
        logger.debug("Waiting for visibility [timeout={}s]: {}", defaultTimeout, locator);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForVisibility(By locator, long timeoutSeconds) {
        logger.debug("Waiting for visibility [timeout={}s]: {}", timeoutSeconds, locator);
        return customWait(timeoutSeconds).until(
                ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForVisibility(WebElement element) {
        logger.debug("Waiting for visibility of WebElement");
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    // ── Clickability ──────────────────────────────────────────────────────────

    public WebElement waitForClickability(By locator) {
        logger.debug("Waiting for clickability [timeout={}s]: {}", defaultTimeout, locator);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement waitForClickability(By locator, long timeoutSeconds) {
        logger.debug("Waiting for clickability [timeout={}s]: {}", timeoutSeconds, locator);
        return customWait(timeoutSeconds).until(
                ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement waitForClickability(WebElement element) {
        logger.debug("Waiting for clickability of WebElement");
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    // ── Presence ──────────────────────────────────────────────────────────────

    public WebElement waitForPresence(By locator) {
        logger.debug("Waiting for presence [timeout={}s]: {}", defaultTimeout, locator);
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public WebElement waitForPresence(By locator, long timeoutSeconds) {
        logger.debug("Waiting for presence [timeout={}s]: {}", timeoutSeconds, locator);
        return customWait(timeoutSeconds).until(
                ExpectedConditions.presenceOfElementLocated(locator));
    }

    // ── Invisibility ──────────────────────────────────────────────────────────

    public boolean waitForInvisibility(By locator) {
        logger.debug("Waiting for invisibility [timeout={}s]: {}", defaultTimeout, locator);
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public boolean waitForInvisibility(By locator, long timeoutSeconds) {
        logger.debug("Waiting for invisibility [timeout={}s]: {}", timeoutSeconds, locator);
        return customWait(timeoutSeconds).until(
                ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ── Staleness ─────────────────────────────────────────────────────────────

    public boolean waitForStaleness(WebElement element) {
        logger.debug("Waiting for staleness of WebElement [timeout={}s]", defaultTimeout);
        return wait.until(ExpectedConditions.stalenessOf(element));
    }

    // ── Element count ─────────────────────────────────────────────────────────

    public List<WebElement> waitForNumberOfElements(By locator, int expectedCount) {
        logger.debug("Waiting for {} element(s) [timeout={}s]: {}", expectedCount, defaultTimeout, locator);
        return wait.until(ExpectedConditions.numberOfElementsToBe(locator, expectedCount));
    }

    // ── Attribute ─────────────────────────────────────────────────────────────

    public boolean waitForAttributeContains(By locator, String attribute, String value) {
        logger.debug("Waiting for attribute '{}' to contain '{}' [timeout={}s]: {}",
                attribute, value, defaultTimeout, locator);
        return wait.until(ExpectedConditions.attributeContains(locator, attribute, value));
    }

    // ── URL conditions ────────────────────────────────────────────────────────

    public boolean waitForUrlContains(String fragment) {
        logger.debug("Waiting for URL to contain: '{}'", fragment);
        return wait.until(ExpectedConditions.urlContains(fragment));
    }

    public boolean waitForUrlToBe(String expectedUrl) {
        logger.debug("Waiting for URL to be: '{}'", expectedUrl);
        return wait.until(ExpectedConditions.urlToBe(expectedUrl));
    }

    // ── Text conditions ───────────────────────────────────────────────────────

    public boolean waitForTextPresent(By locator, String text) {
        logger.debug("Waiting for text '{}' in element: {}", text, locator);
        return wait.until(
                ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public boolean waitForTextPresent(WebElement element, String text) {
        logger.debug("Waiting for text '{}' in WebElement", text);
        return wait.until(
                ExpectedConditions.textToBePresentInElement(element, text));
    }

    // ── All elements visible ──────────────────────────────────────────────────

    public List<WebElement> waitForAllVisible(By locator) {
        logger.debug("Waiting for all elements visible [timeout={}s]: {}", defaultTimeout, locator);
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    // ── Page load ─────────────────────────────────────────────────────────────
    //
    // Waits until document.readyState === 'complete'.
    // Confirms DOM parsing and synchronous resource loading are finished.
    // Does not guarantee AJAX-driven content has rendered — follow with an
    // element-level wait when dynamic content is expected after navigation.
    //
    // Returns true when page load is confirmed. If the condition is not met
    // within the timeout, TimeoutException is thrown — this method never
    // returns false.

    public boolean waitForPageLoad() {
        logger.debug("Waiting for document.readyState to be complete");
        return wait.until(webDriver ->
                "complete".equals(((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")));
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private WebDriverWait customWait(long timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }
}
