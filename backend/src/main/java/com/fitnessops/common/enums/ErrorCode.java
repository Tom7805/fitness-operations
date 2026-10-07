package com.fitnessops.common.enums;

import org.springframework.http.HttpStatus;

/**
 * Mã lỗi ổn định trả cho giao diện. Thông báo mặc định là câu tiếng Việt hiển thị được ngay cho người dùng.
 * Danh mục đầy đủ: docs/api/error-codes.md.
 */
public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên không hợp lệ."),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Yêu cầu không đúng định dạng."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập để tiếp tục."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện chức năng này."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu yêu cầu."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Phương thức không được hỗ trợ."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống gặp lỗi. Vui lòng thử lại sau."),

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng."),
    ACCOUNT_TEMPORARILY_LOCKED(HttpStatus.LOCKED,
            "Tài khoản tạm khóa do nhập sai mật khẩu nhiều lần liên tiếp. Vui lòng thử lại sau."),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên."),
    DEVICE_NOT_REGISTERED(HttpStatus.FORBIDDEN,
            "Máy chưa được đăng ký với câu lạc bộ. Vui lòng nhờ quản lý câu lạc bộ đăng nhập để đăng ký máy trước khi dùng."),
    DEVICE_BRANCH_NOT_ASSIGNED(HttpStatus.FORBIDDEN,
            "Tài khoản không được giao làm việc tại câu lạc bộ của máy quầy này."),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED,
            "Phiên làm việc đã hết hạn do không thao tác trong 30 phút. Vui lòng đăng nhập lại."),
    SESSION_INVALID(HttpStatus.UNAUTHORIZED,
            "Phiên làm việc không hợp lệ hoặc đã kết thúc. Vui lòng đăng nhập lại."),

    BRANCH_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy câu lạc bộ hoặc câu lạc bộ đã ngừng hoạt động."),
    BRANCH_NOT_IN_SCOPE(HttpStatus.FORBIDDEN, "Bạn không được giao quản lý câu lạc bộ này."),
    DEVICE_NAME_DUPLICATE(HttpStatus.CONFLICT, "Câu lạc bộ đã có máy quầy mang tên này.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
