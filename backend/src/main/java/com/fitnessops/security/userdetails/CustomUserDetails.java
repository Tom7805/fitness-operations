package com.fitnessops.security.userdetails;

import com.fitnessops.common.constant.SecurityConstants;
import com.fitnessops.modules.auth.enums.ClientType;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Người dùng đã xác thực của một yêu cầu, dựng lại từ phiên lưu ở máy chủ cho mỗi yêu cầu.
 *
 * @param userId         mã tài khoản
 * @param username       tên đăng nhập
 * @param fullName       họ tên
 * @param roleCodes      mã vai trò
 * @param allBranches    phạm vi toàn chuỗi
 * @param branchIds      câu lạc bộ được giao khi không có phạm vi toàn chuỗi
 * @param sessionId      mã phiên
 * @param clientType     loại thiết bị của phiên
 * @param deviceId       máy quầy mà phiên gắn với, có thể {@code null}
 * @param activeBranchId câu lạc bộ đang làm việc, có thể {@code null}
 */
public record CustomUserDetails(
        Long userId,
        String username,
        String fullName,
        Set<String> roleCodes,
        boolean allBranches,
        Set<Long> branchIds,
        UUID sessionId,
        ClientType clientType,
        Long deviceId,
        Long activeBranchId) implements UserDetails {

    public CustomUserDetails {
        roleCodes = Set.copyOf(roleCodes);
        branchIds = Set.copyOf(branchIds);
    }

    public boolean hasRole(String roleCode) {
        return roleCodes.contains(roleCode);
    }

    public boolean canWorkAtBranch(Long branchId) {
        return allBranches || branchIds.contains(branchId);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = roleCodes.stream()
                .map(code -> (GrantedAuthority) new SimpleGrantedAuthority(SecurityConstants.ROLE_PREFIX + code))
                .toList();
        return authorities;
    }

    /** Không dùng: mật khẩu chỉ được kiểm tra lúc đăng nhập và không bao giờ nằm trong principal. */
    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
