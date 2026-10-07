package com.fitnessops.modules.branch.service;

import com.fitnessops.modules.branch.dto.response.BranchResponse;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Cổng truy cập câu lạc bộ cho các module khác. */
public interface BranchService {

    /** Câu lạc bộ đang hoạt động theo mã, rỗng nếu không có hoặc đã ngừng hoạt động. */
    Optional<BranchResponse> findActiveById(Long id);

    /** Mọi câu lạc bộ đang hoạt động, sắp theo tên. */
    List<BranchResponse> findAllActive();

    /** Câu lạc bộ đang hoạt động trong danh sách mã, sắp theo tên. */
    List<BranchResponse> findActiveByIds(Collection<Long> ids);

    /** Câu lạc bộ theo mã, kể cả đã ngừng hoạt động (dùng để hiển thị dữ liệu lịch sử). */
    Optional<BranchResponse> findById(Long id);
}
