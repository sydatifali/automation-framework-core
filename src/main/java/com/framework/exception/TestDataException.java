package com.framework.exception;

public class TestDataException extends FrameworkException {

    public TestDataException(String message) {
        super(message);
    }

    public TestDataException(String message, Throwable cause) {
        super(message, cause);
    }

    // Pinpoints the exact file and row where the data problem occurred.
    // row is 1-based (matches spreadsheet row numbers, not array indices).
    public TestDataException(String filePath, int row, String detail) {
        super(String.format("Test data error in '%s' at row %d: %s", filePath, row, detail));
    }
}
