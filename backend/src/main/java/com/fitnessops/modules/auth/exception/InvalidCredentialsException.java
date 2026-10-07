package com.fitnessops.modules.auth.exception;

import com.fitnessops.common.enums.ErrorCode;
import com.fitnessops.common.exception.BusinessException;

/** Sai tên đăng nhập hoặc mật khẩu. Cùng một thông báo cho cả hai trường hợp để không lộ tài khoản nào tồn tại. */
public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
