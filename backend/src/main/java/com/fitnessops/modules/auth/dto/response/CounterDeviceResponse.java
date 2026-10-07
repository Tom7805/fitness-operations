package com.fitnessops.modules.auth.dto.response;

import com.fitnessops.modules.branch.dto.response.BranchResponse;
import java.time.Instant;

/** Máy quầy lễ tân đã đăng ký. */
public record CounterDeviceResponse(Long id, String name, BranchResponse branch, Instant registeredAt) {
}
