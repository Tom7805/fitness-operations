package com.fitnessops.modules.auth.dto.response;

/**
 * Trạng thái của máy đang gửi yêu cầu.
 *
 * @param registered máy đã được đăng ký làm máy quầy và còn hiệu lực
 * @param device     thông tin máy quầy, chỉ có khi {@code registered}
 */
public record CurrentDeviceResponse(boolean registered, CounterDeviceResponse device) {

    public static CurrentDeviceResponse notRegistered() {
        return new CurrentDeviceResponse(false, null);
    }
}
