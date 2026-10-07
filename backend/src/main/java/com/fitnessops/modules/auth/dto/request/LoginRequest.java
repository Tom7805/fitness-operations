package com.fitnessops.modules.auth.dto.request;

import com.fitnessops.modules.auth.enums.ClientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Yêu cầu đăng nhập.
 *
 * @param username   tên đăng nhập, không phân biệt hoa thường
 * @param password   mật khẩu
 * @param clientType {@code OFFICE} hoặc {@code MOBILE} do giao diện tự nhận biết; phiên máy quầy do máy chủ
 *                   xác định qua mã máy quầy nên giá trị {@code COUNTER} gửi lên không có tác dụng
 */
public record LoginRequest(
        @NotBlank(message = "Vui lòng nhập tên đăng nhập.")
        @Size(max = 50, message = "Tên đăng nhập tối đa 50 ký tự.")
        String username,

        @NotBlank(message = "Vui lòng nhập mật khẩu.")
        @Size(max = 128, message = "Mật khẩu tối đa 128 ký tự.")
        String password,

        ClientType clientType) {

    /** Không bao giờ để mật khẩu lọt vào log. */
    @Override
    public String toString() {
        return "LoginRequest[username=" + username + ", password=***, clientType=" + clientType + "]";
    }
}
