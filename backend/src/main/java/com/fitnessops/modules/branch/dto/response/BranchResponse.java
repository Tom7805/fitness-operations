package com.fitnessops.modules.branch.dto.response;

import com.fitnessops.modules.branch.entity.Branch;

/** Thông tin tóm tắt của một câu lạc bộ. */
public record BranchResponse(Long id, String code, String name) {

    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getCode(), branch.getName());
    }
}
