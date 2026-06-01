package com.framework.utils;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtils {

    private static final Logger logger = LoggerUtils.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR_KEY     = "screenshot.output.path";
    private static final String DEFAULT_SCREENSHOT_DIR = "target/screenshots";
    private static final DateTimeFormatter TIMESTAMP   =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {}

    // Captures a viewport screenshot for the current browser state.
    // Returns the absolute file path on success, or an empty string if capture fails.
    // This method never throws — a screenshot failure must never affect test outcome.
    public static String captureScreenshot(WebDriver driver, String testName) {
        String screenshotDir = ConfigReader.getInstance()
                .get(SCREENSHOT_DIR_KEY, DEFAULT_SCREENSHOT_DIR);
        String fileName  = buildFileName(testName);
        String targetPath = FileUtils.buildPath(screenshotDir, fileName);

        try {
            FileUtils.ensureDirectoryExists(screenshotDir);
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(srcFile.toPath(), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Screenshot saved: {}", targetPath);
            return targetPath;
        } catch (Exception e) {
            logger.error("Screenshot capture failed for test '{}': {}", testName, e.getMessage(), e);
            return "";
        }
    }

    // Filename includes timestamp (millisecond precision) and thread ID.
    // Together these eliminate collision risk in parallel execution where multiple
    // threads may fail within the same millisecond.
    private static String buildFileName(String testName) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP);
        long   threadId  = Thread.currentThread().getId();
        return FileUtils.sanitizeFileName(testName) + "_" + timestamp + "_t" + threadId + ".png";
    }
}
