package com.fitnessops.common.logging;

/** Làm sạch dữ liệu do người dùng gửi lên trước khi ghi log, chặn chèn dòng log giả (log injection). */
public final class LogSanitizer {

    private static final int MAX_LENGTH = 300;

    private LogSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String singleLine = value.replaceAll("[\\r\\n\\t\\p{Cntrl}]", "_");
        return singleLine.length() <= MAX_LENGTH ? singleLine : singleLine.substring(0, MAX_LENGTH) + "…";
    }
}
