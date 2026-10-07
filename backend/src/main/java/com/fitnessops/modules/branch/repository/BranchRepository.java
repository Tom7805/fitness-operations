package com.fitnessops.modules.branch.repository;

import com.fitnessops.modules.branch.entity.Branch;
import com.fitnessops.modules.branch.enums.BranchStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByIdAndStatus(Long id, BranchStatus status);

    List<Branch> findAllByStatusOrderByNameAsc(BranchStatus status);

    List<Branch> findAllByIdInOrderByNameAsc(Collection<Long> ids);
}
