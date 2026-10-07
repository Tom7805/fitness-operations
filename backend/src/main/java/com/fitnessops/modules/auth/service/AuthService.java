package com.fitnessops.modules.auth.service;

import com.fitnessops.common.web.ClientRequestInfo;
import com.fitnessops.modules.auth.dto.request.LoginRequest;
import com.fitnessops.modules.auth.dto.response.AuthResponse;

/** Đăng nhập hệ thống (NCL-01-CN-001). */
public interface AuthService {

    /**
     * Xác thực tên đăng nhập và mật khẩu, áp quy tắc tạm khóa, máy quầy và phạm vi câu lạc bộ, mở phiên.
     * Mọi lần xử lý — thành công hay thất bại — đều được ghi nhật ký.
     *
     * @throws com.fitnessops.modules.auth.exception.InvalidCredentialsException sai tên đăng nhập hoặc mật khẩu
     * @throws com.fitnessops.modules.auth.exception.AccountLockedException      tài khoản đang tạm khóa
     * @throws com.fitnessops.common.exception.BusinessException                tài khoản bị khóa hoặc vi phạm máy quầy
     */
    AuthResponse login(LoginRequest request, ClientRequestInfo client);
}
