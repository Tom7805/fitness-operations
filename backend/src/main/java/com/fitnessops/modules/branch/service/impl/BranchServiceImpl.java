package com.fitnessops.modules.branch.service.impl;

import com.fitnessops.modules.branch.dto.response.BranchResponse;
import com.fitnessops.modules.branch.entity.Branch;
import com.fitnessops.modules.branch.enums.BranchStatus;
import com.fitnessops.modules.branch.repository.BranchRepository;
import com.fitnessops.modules.branch.service.BranchService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    public Optional<BranchResponse> findActiveById(Long id) {
        return branchRepository.findByIdAndStatus(id, BranchStatus.ACTIVE).map(BranchResponse::from);
    }

    @Override
    public List<BranchResponse> findAllActive() {
        return branchRepository.findAllByStatusOrderByNameAsc(BranchStatus.ACTIVE).stream()
                .map(BranchResponse::from)
                .toList();
    }

    @Override
    public List<BranchResponse> findActiveByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return branchRepository.findAllByIdInOrderByNameAsc(ids).stream()
                .filter(Branch::isActive)
                .map(BranchResponse::from)
                .toList();
    }

    @Override
    public Optional<BranchResponse> findById(Long id) {
        return branchRepository.findById(id).map(BranchResponse::from);
    }
}
