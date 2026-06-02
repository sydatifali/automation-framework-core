package com.framework.utils;

import com.framework.exception.TestDataException;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class TestDataProvider {

    private static final Logger logger = LoggerUtils.getLogger(TestDataProvider.class);

    private TestDataProvider() {}

    // Loads all data rows from a CSV classpath resource and returns them as a
    // TestNG-compatible Object[][] where each element is a single-entry array
    // containing an unmodifiable Map<String, String> keyed by column header name.
    //
    // The test method must accept a single Map<String, String> parameter:
    //
    //   @DataProvider(name = "orderData")
    //   public Object[][] orderData() {
    //       return TestDataProvider.fromCsv("testdata/orders.csv");
    //   }
    //
    //   @Test(dataProvider = "orderData")
    //   public void testOrder(Map<String, String> data) {
    //       String orderId = data.get("orderId");
    //   }
    //
    // Throws TestDataException if the path is invalid, the file is missing,
    // headers are duplicated, parsing fails, or the file contains no data rows.
    public static Object[][] fromCsv(String classpathPath) {
        List<Map<String, String>> rows = CsvReader.read(classpathPath);

        if (rows.isEmpty()) {
            throw new TestDataException(
                    "Test data file '" + classpathPath + "' contains no data rows. " +
                    "At least one data row below the header is required.");
        }

        logger.debug("Prepared {} TestNG data row(s) from: {}", rows.size(), classpathPath);
        return toObjectArray(rows);
    }

    // Loads data rows from a CSV classpath resource and applies a predicate filter
    // before returning. Only rows where the predicate returns true are included.
    //
    // Usage — filter by a column value:
    //
    //   @DataProvider(name = "activeOrders")
    //   public Object[][] activeOrders() {
    //       return TestDataProvider.fromCsv(
    //           "testdata/orders.csv",
    //           row -> "true".equalsIgnoreCase(row.get("active"))
    //       );
    //   }
    //
    // Usage — filter by multiple conditions:
    //
    //   return TestDataProvider.fromCsv(
    //       "testdata/orders.csv",
    //       row -> "OPEN".equals(row.get("status")) && "QA".equals(row.get("environment"))
    //   );
    //
    // Usage — skip rows flagged for exclusion:
    //
    //   return TestDataProvider.fromCsv(
    //       "testdata/orders.csv",
    //       row -> !"true".equalsIgnoreCase(row.get("skip"))
    //   );
    //
    // Throws TestDataException if the path is invalid, the file is missing,
    // headers are duplicated, parsing fails, or no rows match the filter.
    // The total row count is included in the exception message to help distinguish
    // an over-restrictive predicate from an empty file.
    public static Object[][] fromCsv(String classpathPath, Predicate<Map<String, String>> filter) {
        List<Map<String, String>> allRows = CsvReader.read(classpathPath);

        List<Map<String, String>> filtered = allRows.stream()
                .filter(filter)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            throw new TestDataException(
                    "Test data file '" + classpathPath + "' contains no rows matching the supplied filter. " +
                    "Total rows in file: " + allRows.size() + ". " +
                    "Verify the filter predicate and the data file contents.");
        }

        logger.debug("Prepared {}/{} TestNG data row(s) from: {} (filter applied)",
                filtered.size(), allRows.size(), classpathPath);
        return toObjectArray(filtered);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static Object[][] toObjectArray(List<Map<String, String>> rows) {
        Object[][] result = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            result[i][0] = Collections.unmodifiableMap(rows.get(i));
        }
        return result;
    }
}
