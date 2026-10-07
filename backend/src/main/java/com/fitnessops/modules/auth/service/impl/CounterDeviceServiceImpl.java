package com.fitnessops.modules.auth.service.impl;

import com.fitnessops.common.enums.ErrorCode;
import com.fitnessops.common.exception.BusinessException;
import com.fitnessops.common.web.ClientRequestInfo;
import com.fitnessops.modules.auth.dto.request.DeviceRegistrationRequest;
import com.fitnessops.modules.auth.dto.response.CounterDeviceResponse;
import com.fitnessops.modules.auth.dto.response.CurrentDeviceResponse;
import com.fitnessops.modules.auth.dto.response.DeviceRegistrationResponse;
import com.fitnessops.modules.auth.entity.CounterDevice;
import com.fitnessops.modules.auth.enums.AuthEventType;
import com.fitnessops.modules.auth.enums.DeviceStatus;
import com.fitnessops.modules.auth.repository.CounterDeviceRepository;
import com.fitnessops.modules.auth.service.AuthAuditEvent;
import com.fitnessops.modules.auth.service.AuthAuditService;
import com.fitnessops.modules.auth.service.CounterDeviceService;
import com.fitnessops.modules.auth.service.DeviceTokens;
import com.fitnessops.modules.branch.dto.response.BranchResponse;
import com.fitnessops.modules.branch.service.BranchService;
import com.fitnessops.modules.user.enums.RoleName;
import com.fitnessops.security.userdetails.CustomUserDetails;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CounterDeviceServiceImpl implements CounterDeviceService {

    private final CounterDeviceRepository deviceRepository;
    private final BranchService branchService;
    private final AuthAuditService auditService;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Optional<CounterDevice> findActiveByToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return deviceRepository.findByTokenHashAndStatus(DeviceTokens.hash(rawToken), DeviceStatus.ACTIVE)
                .filter(device -> branchService.findActiveById(device.getBranchId()).isPresent());
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentDeviceResponse describeCurrent(String rawToken) {
        return findActiveByToken(rawToken)
                .map(device -> new CurrentDeviceResponse(true, toResponse(device)))
                .orElseGet(CurrentDeviceResponse::notRegistered);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> registrableBranches(CustomUserDetails principal) {
        if (hasChainWideDeviceRights(principal)) {
            return branchService.findAllActive();
        }
        return branchService.findActiveByIds(principal.branchIds());
    }

    @Override
    @Transactional
    public DeviceRegistrationResponse register(DeviceRegistrationRequest request, CustomUserDetails principal,
                                               ClientRequestInfo client) {
        BranchResponse branch = branchService.findActiveById(request.branchId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BRANCH_NOT_FOUND));
        if (!hasChainWideDeviceRights(principal) && !principal.canWorkAtBranch(branch.id())) {
            throw new BusinessException(ErrorCode.BRANCH_NOT_IN_SCOPE);
        }

        String name = request.name().trim().replaceAll("\\s+", " ");
        if (deviceRepository.existsByBranchAndNameIgnoreCase(branch.id(), name, DeviceStatus.ACTIVE)) {
            throw duplicateName();
        }

        Instant now = Instant.now(clock);
        // Đăng ký lại một máy đã là máy quầy: thu hồi mã cũ để máy chỉ còn một danh tính.
        Optional<CounterDevice> previous = findActiveByToken(client.deviceToken());
        previous.ifPresent(device -> device.revoke(now));

        String rawToken = DeviceTokens.generate();
        CounterDevice device;
        try {
            device = deviceRepository.saveAndFlush(
                    CounterDevice.register(branch.id(), name, DeviceTokens.hash(rawToken), principal.userId(), now));
        } catch (DataIntegrityViolationException ex) {
            throw duplicateName();
        }

        String detail = "Đăng ký máy quầy \"" + name + "\" cho " + branch.name()
                + previous.map(old -> " (thay cho máy quầy \"" + old.getName() + "\")").orElse("");
        auditService.record(AuthAuditEvent.builder()
                .type(AuthEventType.DEVICE_REGISTERED)
                .userId(principal.userId())
                .username(principal.username())
                .sessionId(principal.sessionId())
                .clientType(principal.clientType())
                .deviceId(device.getId())
                .branchId(branch.id())
                .client(client)
                .detail(detail)
                .build());

        return new DeviceRegistrationResponse(new CounterDeviceResponse(device.getId(), device.getName(), branch,
                device.getRegisteredAt()), rawToken);
    }

    @Override
    @Transactional(readOnly = true)
    public CounterDeviceResponse toResponse(CounterDevice device) {
        BranchResponse branch = branchService.findById(device.getBranchId()).orElse(null);
        return new CounterDeviceResponse(device.getId(), device.getName(), branch, device.getRegisteredAt());
    }

    /** Quản trị viên cấu hình mọi câu lạc bộ; quản lý có phạm vi toàn chuỗi cũng vậy. */
    private static boolean hasChainWideDeviceRights(CustomUserDetails principal) {
        return principal.hasRole(RoleName.ADMIN.name()) || principal.allBranches();
    }

    private static BusinessException duplicateName() {
        return new BusinessException(ErrorCode.DEVICE_NAME_DUPLICATE);
    }
}
