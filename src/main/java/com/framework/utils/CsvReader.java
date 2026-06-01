package com.framework.utils;

import com.framework.exception.TestDataException;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CsvReader {

    private static final Logger logger = LoggerUtils.getLogger(CsvReader.class);

    private CsvReader() {}

    // Reads all data rows from a classpath resource.
    // classpathPath must be relative to the classpath root, e.g. "testdata/users.csv".
    // Returns each row as a Map<header, value>; throws TestDataException on any failure.
    public static List<Map<String, String>> read(String classpathPath) {
        logger.debug("Loading test data from classpath: {}", classpathPath);

        InputStream stream = CsvReader.class.getClassLoader().getResourceAsStream(classpathPath);
        if (stream == null) {
            throw new TestDataException(
                    "Test data file not found on classpath: '" + classpathPath + "'. " +
                    "Ensure the file exists under src/test/resources/.");
        }

        List<Map<String, String>> rows = new ArrayList<>();
        int rowNumber = 1; // 1-based; row 1 is the header consumed by CSVReaderHeaderAware

        try (CSVReaderHeaderAware csvReader =
                     new CSVReaderHeaderAware(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            Map<String, String> row;
            while ((row = csvReader.readMap()) != null) {
                rowNumber++;
                rows.add(row);
            }

        } catch (CsvValidationException e) {
            throw new TestDataException(
                    String.format("Test data error in '%s' at row %d: CSV parse error: %s",
                            classpathPath, rowNumber, e.getMessage()), e);
        } catch (IOException e) {
            throw new TestDataException(
                    String.format("Test data error in '%s' at row %d: I/O error: %s",
                            classpathPath, rowNumber, e.getMessage()), e);
        }

        logger.info("Loaded {} data row(s) from: {}", rows.size(), classpathPath);
        return rows;
    }
}
