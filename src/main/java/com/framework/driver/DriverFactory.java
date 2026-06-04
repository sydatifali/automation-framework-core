package com.framework.driver;

import com.framework.exception.ConfigurationException;
import com.framework.exception.DriverInitialisationException;
import com.framework.utils.ConfigReader;
import com.framework.utils.LoggerUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Set;

public final class DriverFactory {

    private static final Logger logger = LoggerUtils.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ConfigReader config = ConfigReader.getInstance();

    private static final Set<String> SUPPORTED_BROWSERS = Set.of("chrome", "firefox", "edge");

    private DriverFactory() {}

    public static WebDriver getDriver() {
        if (driverThreadLocal.get() == null) {
            driverThreadLocal.set(initialiseDriver());
        }
        return driverThreadLocal.get();
    }

    public static boolean hasDriver() {
        return driverThreadLocal.get() != null;
    }

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver session terminated");
            } catch (Exception e) {
                logger.warn("Exception while quitting WebDriver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    private static WebDriver initialiseDriver() {
        String executionMode = config.get("execution.mode", "local");
        String browser       = config.get("browser", "chrome").toLowerCase().trim();
        boolean headless     = config.getBoolean("browser.headless", false);

        validateBrowser(browser);
        logStartupConfig(executionMode, browser, headless);

        WebDriver driver = switch (executionMode.toLowerCase().trim()) {
            case "remote"  -> createRemoteDriver(browser);
            default        -> createLocalDriver(browser);
        };

        configureDriver(driver);
        return driver;
    }

    // ── Local execution ──────────────────────────────────────────────────────

    private static WebDriver createLocalDriver(String browser) {
        return switch (browser) {
            case "chrome" -> {
                WebDriverManager.chromedriver().setup();
                logger.debug("Chrome driver binary resolved via WebDriverManager");
                yield new ChromeDriver(buildChromeOptions());
            }
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                logger.debug("Firefox driver binary resolved via WebDriverManager");
                yield new FirefoxDriver(buildFirefoxOptions());
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                logger.debug("Edge driver binary resolved via WebDriverManager");
                yield new EdgeDriver(buildEdgeOptions());
            }
            default -> throw new DriverInitialisationException(unsupportedBrowserMessage(browser));
        };
    }

    // ── Remote execution (Selenium Grid) ─────────────────────────────────────

    private static WebDriver createRemoteDriver(String browser) {
        URL gridUrl = resolveGridUrl();
        logger.info("Connecting to remote hub: {}", gridUrl);
        return switch (browser) {
            case "chrome"  -> new RemoteWebDriver(gridUrl, buildChromeOptions());
            case "firefox" -> new RemoteWebDriver(gridUrl, buildFirefoxOptions());
            case "edge"    -> new RemoteWebDriver(gridUrl, buildEdgeOptions());
            default        -> throw new DriverInitialisationException(unsupportedBrowserMessage(browser));
        };
    }

    // ── Browser options ───────────────────────────────────────────────────────

    private static ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080"
        );
        if (config.getBoolean("browser.headless", false)) {
            options.addArguments("--headless=new");
        }
        applyDownloadDirectory(options);
        return options;
    }

    private static FirefoxOptions buildFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1920", "--height=1080");
        if (config.getBoolean("browser.headless", false)) {
            options.addArguments("--headless");
        }
        return options;
    }

    private static EdgeOptions buildEdgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080"
        );
        if (config.getBoolean("browser.headless", false)) {
            options.addArguments("--headless=new");
        }
        applyDownloadDirectory(options);
        return options;
    }

    // ── Download directory integration point ─────────────────────────────────
    //
    // When browser.download.dir is set in config, Chrome and Edge will route
    // file downloads to that directory automatically. Firefox requires a
    // different mechanism (profile preferences) and will be wired here when
    // Firefox download support is added.
    //
    // Usage: set browser.download.dir=target/downloads in config.properties
    // or override via BROWSER_DOWNLOAD_DIR environment variable.

    private static void applyDownloadDirectory(ChromeOptions options) {
        String downloadDir = config.get("browser.download.dir");
        if (downloadDir != null && !downloadDir.isBlank()) {
            options.addArguments("--download-default-directory=" + downloadDir.trim());
            logger.debug("Chrome download directory set: {}", downloadDir.trim());
        }
    }

    private static void applyDownloadDirectory(EdgeOptions options) {
        String downloadDir = config.get("browser.download.dir");
        if (downloadDir != null && !downloadDir.isBlank()) {
            options.addArguments("--download-default-directory=" + downloadDir.trim());
            logger.debug("Edge download directory set: {}", downloadDir.trim());
        }
    }

    // ── Driver configuration ──────────────────────────────────────────────────

    private static void configureDriver(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(config.getLong("driver.page.load.timeout", 30)));
        driver.manage().timeouts().scriptTimeout(
                Duration.ofSeconds(config.getLong("driver.script.timeout", 30)));
        try {
            driver.manage().window().maximize();
        } catch (Exception e) {
            logger.warn("Window maximise not supported in this environment: {}", e.getMessage());
        }
    }

    // ── Validation and helpers ────────────────────────────────────────────────

    private static void validateBrowser(String browser) {
        if (!SUPPORTED_BROWSERS.contains(browser)) {
            throw new DriverInitialisationException(unsupportedBrowserMessage(browser));
        }
    }

    private static String unsupportedBrowserMessage(String browser) {
        return String.format(
                "Unsupported browser: '%s'. Supported values are: %s. " +
                "Set the correct value via browser property, BROWSER environment variable, " +
                "or browser key in config.properties.",
                browser, SUPPORTED_BROWSERS);
    }

    private static URL resolveGridUrl() {
        String gridUrl = config.get("grid.url");
        if (gridUrl == null || gridUrl.isBlank()) {
            throw new DriverInitialisationException(
                    "Remote execution requires 'grid.url' to be configured. " +
                    "Set it in config.properties, via the GRID_URL environment variable, " +
                    "or as a -Dgrid.url Maven argument.");
        }
        try {
            return new URL(gridUrl.trim());
        } catch (MalformedURLException e) {
            throw new DriverInitialisationException(
                    "Invalid grid URL: '" + gridUrl.trim() + "'", e);
        }
    }

    private static void logStartupConfig(String executionMode, String browser, boolean headless) {
        logger.info("--------------------------------------------------");
        logger.info("  Execution Mode : {}", executionMode.toUpperCase());
        logger.info("  Browser        : {}", browser);
        logger.info("  Headless       : {}", headless);
        logger.info("  Target         : {}",
                switch (executionMode.toLowerCase().trim()) {
                    case "remote"  -> config.get("grid.url", "not configured");
                    default        -> "local machine";
                });
        logger.info("--------------------------------------------------");
    }
}
