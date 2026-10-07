package com.fitnessops.modules.auth.mapper;

import com.fitnessops.config.properties.AuthProperties;
import com.fitnessops.modules.auth.dto.response.CounterDeviceResponse;
import com.fitnessops.modules.auth.dto.response.RoleSummaryResponse;
import com.fitnessops.modules.auth.dto.response.SessionResponse;
import com.fitnessops.modules.auth.dto.response.SessionUserResponse;
import com.fitnessops.modules.auth.entity.CounterDevice;
import com.fitnessops.modules.auth.entity.UserSession;
import com.fitnessops.modules.auth.service.CounterDeviceService;
import com.fitnessops.modules.branch.dto.response.BranchResponse;
import com.fitnessops.modules.branch.service.BranchService;
import com.fitnessops.modules.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Dựng {@link SessionResponse}: người dùng, vai trò, phạm vi câu lạc bộ, câu lạc bộ đang làm việc, máy quầy. */
@Component
@RequiredArgsConstructor
public class SessionResponseMapper {

    private final BranchService branchService;
    private final CounterDeviceService counterDeviceService;
    private final AuthProperties authProperties;

    /** Phải gọi trong giao dịch để đọc được vai trò và phạm vi của {@code user}. */
    public SessionResponse toResponse(UserSession session, User user, CounterDevice device) {
        List<RoleSummaryResponse> roles = user.sortedRoles().stream()
                .map(role -> new RoleSummaryResponse(role.getCode(), role.getName()))
                .toList();
        List<BranchResponse> branches = user.isAllBranches()
                ? branchService.findAllActive()
                : branchService.findActiveByIds(user.getBranchIds());
        SessionUserResponse sessionUser = new SessionUserResponse(user.getId(), user.getUsername(),
                user.getFullName(), user.getJobTitle(), roles, user.isAllBranches(), branches,
                user.isMustChangePassword());

        BranchResponse activeBranch = session.getBranchId() == null
                ? null
                : branchService.findById(session.getBranchId()).orElse(null);
        CounterDeviceResponse deviceResponse = device == null ? null : counterDeviceService.toResponse(device);

        return new SessionResponse(session.getId(), session.getClientType(), session.getCreatedAt(),
                session.getExpiresAt(), authProperties.idleTimeout().toSeconds(), sessionUser, activeBranch,
                deviceResponse);
    }
}
