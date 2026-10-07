package com.fitnessops.modules.auth.service;

import com.fitnessops.common.web.ClientRequestInfo;
import com.fitnessops.modules.auth.dto.response.SessionResponse;
import com.fitnessops.modules.auth.entity.CounterDevice;
import com.fitnessops.modules.auth.entity.UserSession;
import com.fitnessops.modules.auth.enums.ClientType;
import com.fitnessops.modules.user.entity.User;
import com.fitnessops.security.jwt.JwtClaims;
import com.fitnessops.security.userdetails.CustomUserDetails;
import java.time.Instant;

/** Phiên làm việc lưu ở máy chủ (R5). */
public interface SessionService {

    /** Mở phiên trong giao dịch đăng nhập đang chạy. */
    UserSession open(User user, ClientType clientType, CounterDevice device, Long activeBranchId,
                     ClientRequestInfo client, Instant now);

    /**
     * Kiểm tra phiên cho một yêu cầu: còn mở, chưa quá thời gian không thao tác, đúng máy quầy, tài khoản còn
     * hoạt động. Hợp lệ thì cập nhật lần thao tác cuối; quá thời gian không thao tác thì kết thúc phiên và ghi
     * nhật ký.
     */
    SessionAuthentication authenticate(JwtClaims claims, ClientRequestInfo client);

    SessionResponse describe(CustomUserDetails principal);

    /** Đăng xuất: kết thúc phiên và ghi nhật ký. */
    void logout(CustomUserDetails principal, ClientRequestInfo client);
}
