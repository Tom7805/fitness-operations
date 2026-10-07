package com.fitnessops.modules.auth.controller;

import com.fitnessops.common.constant.ApiPaths;
import com.fitnessops.common.web.ClientRequestInfo;
import com.fitnessops.modules.auth.dto.request.DeviceRegistrationRequest;
import com.fitnessops.modules.auth.dto.response.CurrentDeviceResponse;
import com.fitnessops.modules.auth.dto.response.DeviceRegistrationResponse;
import com.fitnessops.modules.auth.service.CounterDeviceService;
import com.fitnessops.modules.branch.dto.response.BranchResponse;
import com.fitnessops.security.annotation.CurrentUser;
import com.fitnessops.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.DEVICES)
@RequiredArgsConstructor
@Tag(name = "Máy quầy", description = "Đăng ký máy quầy lễ tân với câu lạc bộ")
public class CounterDeviceController {

    private static final String CAN_REGISTER_DEVICE = "hasAnyRole('CLUB_MANAGER', 'ADMIN')";

    private final CounterDeviceService counterDeviceService;

    @GetMapping("/current")
    @Operation(summary = "Máy đang dùng đã được đăng ký làm máy quầy chưa",
            description = "Không cần đăng nhập; đọc mã máy quầy trong header X-Device-Token.")
    public CurrentDeviceResponse current(HttpServletRequest httpRequest) {
        return counterDeviceService.describeCurrent(ClientRequestInfo.from(httpRequest).deviceToken());
    }

    @GetMapping("/registrable-branches")
    @PreAuthorize(CAN_REGISTER_DEVICE)
    @Operation(summary = "Câu lạc bộ người dùng được phép đăng ký máy quầy")
    public List<BranchResponse> registrableBranches(@CurrentUser CustomUserDetails principal) {
        return counterDeviceService.registrableBranches(principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(CAN_REGISTER_DEVICE)
    @Operation(summary = "Đăng ký máy đang dùng làm máy quầy lễ tân",
            description = "Mã máy quầy chỉ trả về một lần; trình duyệt của máy phải lưu lại.")
    public DeviceRegistrationResponse register(@Valid @RequestBody DeviceRegistrationRequest request,
                                               @CurrentUser CustomUserDetails principal,
                                               HttpServletRequest httpRequest) {
        return counterDeviceService.register(request, principal, ClientRequestInfo.from(httpRequest));
    }
}
