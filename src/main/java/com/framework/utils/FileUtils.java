package com.framework.utils;

import com.framework.exception.FrameworkException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FileUtils {

    private static final Logger logger = LoggerUtils.getLogger(FileUtils.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private FileUtils() {}

    public static void ensureDirectoryExists(String directoryPath) {
        try {
            Files.createDirectories(Paths.get(directoryPath));
            logger.debug("Directory confirmed: {}", directoryPath);
        } catch (IOException e) {
            logger.error("Failed to create directory: {}", directoryPath, e);
            throw new FrameworkException("Failed to create directory: " + directoryPath, e);
        }
    }

    public static String buildPath(String directory, String fileName) {
        return Paths.get(directory, fileName).toString();
    }

    public static String timestampedFileName(String baseName, String extension) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String sanitized = sanitizeFileName(baseName);
        String ext = extension.startsWith(".") ? extension : "." + extension;
        return sanitized + "_" + timestamp + ext;
    }

    public static String sanitizeFileName(String name) {
        return StringUtils.defaultIfBlank(name, "unnamed")
                .replaceAll("[^a-zA-Z0-9_\\-]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");
    }
}
