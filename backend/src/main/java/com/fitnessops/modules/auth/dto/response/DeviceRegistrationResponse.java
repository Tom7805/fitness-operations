package com.fitnessops.modules.auth.dto.response;

/**
 * Kết quả đăng ký máy quầy.
 *
 * @param device      máy vừa đăng ký
 * @param deviceToken mã máy quầy, chỉ trả về một lần; trình duyệt của máy phải lưu lại và gửi kèm mọi yêu cầu
 */
public record DeviceRegistrationResponse(CounterDeviceResponse device, String deviceToken) {

    @Override
    public String toString() {
        return "DeviceRegistrationResponse[device=" + device + ", deviceToken=***]";
    }
}
