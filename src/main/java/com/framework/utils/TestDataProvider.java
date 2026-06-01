package com.framework.utils;

import com.framework.exception.TestDataException;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public final class TestDataProvider {

    private static final Logger logger = LoggerUtils.getLogger(TestDataProvider.class);

    private TestDataProvider() {}

    // Loads a CSV file from the classpath and returns its rows as a TestNG-compatible
    // Object[][] where each element is a Map<String, String> (header → value).
    //
    // Usage in a consumer @DataProvider:
    //   @DataProvider(name = "loginData")
    //   public Object[][] loginData() {
    //       return TestDataProvider.fromCsv("testdata/login.csv");
    //   }
    //
    // Test method signature:
    //   @Test(dataProvider = "loginData")
    //   public void testLogin(Map<String, String> data) { ... }
    public static Object[][] fromCsv(String classpathPath) {
        List<Map<String, String>> rows = CsvReader.read(classpathPath);

        if (rows.isEmpty()) {
            throw new TestDataException(
                    "Test data file '" + classpathPath + "' contains no data rows. " +
                    "At least one data row is required.");
        }

        Object[][] result = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            result[i][0] = rows.get(i);
        }

        logger.debug("Prepared {} TestNG data row(s) from: {}", rows.size(), classpathPath);
        return result;
    }
}
