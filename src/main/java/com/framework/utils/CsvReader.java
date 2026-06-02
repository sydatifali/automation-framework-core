package com.framework.utils;

import com.framework.exception.TestDataException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CsvReader {

    private static final Logger logger = LoggerUtils.getLogger(CsvReader.class);

    private CsvReader() {}

    // Reads all data rows from a classpath resource into a List of header-keyed maps.
    // classpathPath is relative to the classpath root: e.g. "testdata/orders.csv".
    //
    // Behaviour contract:
    //   - First row is always consumed as the header (row 1).
    //   - Duplicate column names throw TestDataException before any row is processed.
    //   - Blank column names are logged as a warning; data in blank columns is inaccessible.
    //   - Empty field values are returned as "" (not null).
    //   - Rows shorter than the header produce "" for missing trailing columns.
    //   - Rows longer than the header silently drop extra columns.
    //   - An empty file or header-only file returns an empty List.
    //
    // Row numbers in error messages use 1-based indexing where row 1 is the header.
    //
    // Throws TestDataException on null/blank path, missing file, duplicate headers,
    // CSV parse error, or I/O failure.
    public static List<Map<String, String>> read(String classpathPath) {
        if (classpathPath == null || classpathPath.isBlank()) {
            throw new TestDataException("CSV file path must not be null or blank.");
        }

        logger.debug("Loading test data from classpath: {}", classpathPath);

        InputStream stream = CsvReader.class.getClassLoader().getResourceAsStream(classpathPath);
        if (stream == null) {
            throw new TestDataException(
                    "Test data file not found on classpath: '" + classpathPath + "'. " +
                    "Ensure the file exists under src/test/resources/.");
        }

        List<Map<String, String>> rows = new ArrayList<>();
        int rowNumber = 1;

        try (CSVReader csvReader =
                     new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            // Row 1: header — read as raw String[] for validation before map construction.
            String[] headers = csvReader.readNext();
            if (headers == null) {
                logger.info("Loaded 0 data row(s) from: {} (file is empty)", classpathPath);
                return rows;
            }

            validateNoDuplicateHeaders(headers, classpathPath);
            warnOnBlankHeaders(headers, classpathPath);

            // Rows 2+: data
            String[] rawRow;
            while ((rawRow = csvReader.readNext()) != null) {
                rowNumber++;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    rowMap.put(headers[i], i < rawRow.length ? rawRow[i] : "");
                }
                rows.add(rowMap);
            }

        } catch (CsvValidationException e) {
            throw new TestDataException(
                    String.format("Test data error in '%s' at row %d (row 1 is the header): " +
                                  "CSV parse error: %s", classpathPath, rowNumber, e.getMessage()), e);
        } catch (IOException e) {
            throw new TestDataException(
                    String.format("Test data error in '%s' at row %d (row 1 is the header): " +
                                  "I/O error: %s", classpathPath, rowNumber, e.getMessage()), e);
        }

        logger.info("Loaded {} data row(s) from: {}", rows.size(), classpathPath);
        return rows;
    }

    // ── Validation ────────────────────────────────────────────────────────────

    // Collects all duplicate names before throwing so the error reports every
    // offending column at once rather than stopping at the first duplicate.
    // LinkedHashSet preserves insertion order so duplicates appear in file order.
    private static void validateNoDuplicateHeaders(String[] headers, String classpathPath) {
        Set<String> seen = new LinkedHashSet<>();
        List<String> duplicates = new ArrayList<>();
        for (String header : headers) {
            if (!seen.add(header) && !duplicates.contains(header)) {
                duplicates.add(header);
            }
        }
        if (!duplicates.isEmpty()) {
            throw new TestDataException(
                    "Duplicate column header(s) detected in '" + classpathPath + "': " +
                    duplicates + ". All column names must be unique. " +
                    "Full header row: " + Arrays.toString(headers));
        }
    }

    private static void warnOnBlankHeaders(String[] headers, String classpathPath) {
        for (String header : headers) {
            if (header == null || header.isBlank()) {
                logger.warn(
                        "Blank column name detected in '{}'. " +
                        "Columns must have unique, non-blank header names. " +
                        "Data in blank-named columns cannot be accessed by name and will be lost.",
                        classpathPath);
                return;
            }
        }
    }
}
