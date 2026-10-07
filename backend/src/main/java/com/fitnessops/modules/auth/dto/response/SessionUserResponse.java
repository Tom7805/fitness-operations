package com.fitnessops.modules.auth.dto.response;

import com.fitnessops.modules.branch.dto.response.BranchResponse;
import java.util.List;

/**
 * Người dùng của phiên cùng vai trò và phạm vi câu lạc bộ (QTN-01).
 *
 * @param allBranches {@code true} khi được giao toàn chuỗi; khi đó {@code branches} là mọi câu lạc bộ đang hoạt động
 * @param branches    câu lạc bộ được giao làm việc
 */
public record SessionUserResponse(
        Long id,
        String username,
        String fullName,
        String jobTitle,
        List<RoleSummaryResponse> roles,
        boolean allBranches,
        List<BranchResponse> branches,
        boolean mustChangePassword) {
}
