package com.fitnessops.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Đăng ký máy đang dùng làm máy quầy lễ tân của một câu lạc bộ.
 *
 * @param branchId câu lạc bộ đặt máy
 * @param name     tên máy, duy nhất trong câu lạc bộ (vd. "Quầy lễ tân 1")
 */
public record DeviceRegistrationRequest(
        @NotNull(message = "Vui lòng chọn câu lạc bộ.")
        Long branchId,

        @NotBlank(message = "Vui lòng đặt tên cho máy quầy.")
        @Size(max = 100, message = "Tên máy quầy tối đa 100 ký tự.")
        String name) {
}
