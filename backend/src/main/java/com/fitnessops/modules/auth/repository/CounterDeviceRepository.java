package com.fitnessops.modules.auth.repository;

import com.fitnessops.modules.auth.entity.CounterDevice;
import com.fitnessops.modules.auth.enums.DeviceStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CounterDeviceRepository extends JpaRepository<CounterDevice, Long> {

    Optional<CounterDevice> findByTokenHashAndStatus(String tokenHash, DeviceStatus status);

    @Query("select count(d) > 0 from CounterDevice d "
            + "where d.branchId = :branchId and lower(d.name) = lower(:name) and d.status = :status")
    boolean existsByBranchAndNameIgnoreCase(@Param("branchId") Long branchId, @Param("name") String name,
                                            @Param("status") DeviceStatus status);
}
