package com.fitnessops.common.dto;

/** Lỗi của một trường dữ liệu. */
public record FieldErrorResponse(String field, String message) {
}
