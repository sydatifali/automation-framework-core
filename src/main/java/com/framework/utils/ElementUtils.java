package com.framework.utils;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class ElementUtils {

    private static final Logger logger = LoggerUtils.getLogger(ElementUtils.class);

    private final WebDriver driver;
    private final WaitUtils waitUtils;
    private final JavascriptExecutor jsExecutor;
    private final Actions actions;

    public ElementUtils(WebDriver driver, WaitUtils waitUtils) {
        this.driver     = driver;
        this.waitUtils  = waitUtils;
        this.jsExecutor = (JavascriptExecutor) driver;
        this.actions    = new Actions(driver);
    }

    // ── Click ─────────────────────────────────────────────────────────────────

    public void click(By locator) {
        logger.debug("Click: {}", locator);
        waitUtils.waitForClickability(locator).click();
    }

    public void click(WebElement element) {
        logger.debug("Click: WebElement");
        waitUtils.waitForClickability(element).click();
    }

    // Uses JavaScript click — only for cases where standard click is intercepted.
    // Bypasses browser event propagation; do not use as the default click strategy.
    public void jsClick(By locator) {
        logger.debug("JS click: {}", locator);
        WebElement element = waitUtils.waitForPresence(locator);
        jsExecutor.executeScript("arguments[0].click();", element);
    }

    // ── Text input ────────────────────────────────────────────────────────────

    public void enterText(By locator, String text) {
        logger.debug("Enter text in: {}", locator);
        WebElement element = waitUtils.waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
    }

    public void clear(By locator) {
        logger.debug("Clear field: {}", locator);
        waitUtils.waitForVisibility(locator).clear();
    }

    // ── Text and attribute retrieval ──────────────────────────────────────────

    public String getText(By locator) {
        logger.debug("Get text: {}", locator);
        return waitUtils.waitForVisibility(locator).getText();
    }

    public String getText(WebElement element) {
        logger.debug("Get text: WebElement");
        return waitUtils.waitForVisibility(element).getText();
    }

    public String getAttribute(By locator, String attribute) {
        logger.debug("Get attribute '{}': {}", attribute, locator);
        return waitUtils.waitForPresence(locator).getAttribute(attribute);
    }

    public String getAttribute(WebElement element, String attribute) {
        logger.debug("Get attribute '{}': WebElement", attribute);
        return waitUtils.waitForVisibility(element).getAttribute(attribute);
    }

    // Returns the current value of a form field (input, textarea, select).
    // Equivalent to getAttribute(locator, "value") but named for intent clarity.
    public String getValue(By locator) {
        logger.debug("Get value: {}", locator);
        return waitUtils.waitForPresence(locator).getAttribute("value");
    }

    // ── Dropdown retrieval ────────────────────────────────────────────────────

    public String getSelectedOptionText(By locator) {
        logger.debug("Get selected option text: {}", locator);
        return new Select(waitUtils.waitForVisibility(locator))
                .getFirstSelectedOption()
                .getText();
    }

    // ── State queries ─────────────────────────────────────────────────────────
    //
    // All state query methods use driver.findElements() (plural), not WaitUtils.
    // These methods answer "is the element in this state right now?" — applying
    // a full timeout wait for an absent element is wrong for a state query.
    // findElements() returns an empty list immediately and safely without throwing.
    //
    // isPresent  : element exists in the DOM (visible or hidden)
    // isDisplayed: element exists AND is visible on screen
    // isEnabled  : element exists AND is interactable
    // isSelected : element exists AND is in selected state (checkbox, radio, option)

    public boolean isPresent(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (Exception e) {
            logger.debug("isPresent returned false for {}: {}", locator, e.getMessage());
            return false;
        }
    }

    public boolean isDisplayed(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            return !elements.isEmpty() && elements.get(0).isDisplayed();
        } catch (Exception e) {
            logger.debug("isDisplayed returned false for {}: {}", locator, e.getMessage());
            return false;
        }
    }

    public boolean isEnabled(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            return !elements.isEmpty() && elements.get(0).isEnabled();
        } catch (Exception e) {
            logger.debug("isEnabled returned false for {}: {}", locator, e.getMessage());
            return false;
        }
    }

    public boolean isSelected(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            return !elements.isEmpty() && elements.get(0).isSelected();
        } catch (Exception e) {
            logger.debug("isSelected returned false for {}: {}", locator, e.getMessage());
            return false;
        }
    }

    public int count(By locator) {
        try {
            int size = driver.findElements(locator).size();
            logger.debug("Count: {} element(s) for: {}", size, locator);
            return size;
        } catch (Exception e) {
            logger.debug("count() returned 0 for {}: {}", locator, e.getMessage());
            return 0;
        }
    }

    // ── Mouse interactions ────────────────────────────────────────────────────

    public void hover(By locator) {
        logger.debug("Hover: {}", locator);
        WebElement element = waitUtils.waitForVisibility(locator);
        actions.moveToElement(element).perform();
    }

    // ── Scroll ────────────────────────────────────────────────────────────────
    //
    // behavior:'auto' (instant) is used instead of 'smooth' (animated).
    // Smooth scroll is asynchronous — subsequent interactions would fire
    // before the element reaches its destination.
    //
    // scrollToElement uses waitForPresence, not waitForVisibility — the
    // element may be off-screen (not visible) and scrolling brings it
    // into view. Presence confirms it exists in the DOM.

    public void scrollToElement(By locator) {
        logger.debug("Scroll to element: {}", locator);
        WebElement element = waitUtils.waitForPresence(locator);
        jsExecutor.executeScript(
                "arguments[0].scrollIntoView({behavior:'auto', block:'center'});", element);
    }

    public void scrollToTop() {
        logger.debug("Scroll to top");
        jsExecutor.executeScript("window.scrollTo(0, 0);");
    }

    public void scrollToBottom() {
        logger.debug("Scroll to bottom");
        jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    // ── Select (dropdown) ─────────────────────────────────────────────────────

    public void selectByVisibleText(By locator, String text) {
        logger.debug("Select by visible text '{}': {}", text, locator);
        new Select(waitUtils.waitForVisibility(locator)).selectByVisibleText(text);
    }

    public void selectByValue(By locator, String value) {
        logger.debug("Select by value '{}': {}", value, locator);
        new Select(waitUtils.waitForVisibility(locator)).selectByValue(value);
    }

    public void selectByIndex(By locator, int index) {
        logger.debug("Select by index {}: {}", index, locator);
        new Select(waitUtils.waitForVisibility(locator)).selectByIndex(index);
    }
}
