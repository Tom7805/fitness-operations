package com.fitnessops.modules.auth.service;

import com.fitnessops.common.web.ClientRequestInfo;
import com.fitnessops.modules.auth.dto.request.DeviceRegistrationRequest;
import com.fitnessops.modules.auth.dto.response.CounterDeviceResponse;
import com.fitnessops.modules.auth.dto.response.CurrentDeviceResponse;
import com.fitnessops.modules.auth.dto.response.DeviceRegistrationResponse;
import com.fitnessops.modules.auth.entity.CounterDevice;
import com.fitnessops.modules.branch.dto.response.BranchResponse;
import com.fitnessops.security.userdetails.CustomUserDetails;
import java.util.List;
import java.util.Optional;

/** Máy quầy lễ tân: nhận diện máy đang gửi yêu cầu và đăng ký máy mới với câu lạc bộ. */
public interface CounterDeviceService {

    /** Máy quầy đang hoạt động (và câu lạc bộ của máy đang hoạt động) ứng với mã máy quầy, nếu có. */
    Optional<CounterDevice> findActiveByToken(String rawToken);

    CurrentDeviceResponse describeCurrent(String rawToken);

    /** Câu lạc bộ mà người dùng được phép đăng ký máy quầy. */
    List<BranchResponse> registrableBranches(CustomUserDetails principal);

    DeviceRegistrationResponse register(DeviceRegistrationRequest request, CustomUserDetails principal,
                                        ClientRequestInfo client);

    CounterDeviceResponse toResponse(CounterDevice device);
}
