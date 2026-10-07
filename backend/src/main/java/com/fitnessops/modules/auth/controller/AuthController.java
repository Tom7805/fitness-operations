package com.fitnessops.modules.auth.controller;

import com.fitnessops.common.constant.ApiPaths;
import com.fitnessops.common.web.ClientRequestInfo;
import com.fitnessops.modules.auth.dto.request.LoginRequest;
import com.fitnessops.modules.auth.dto.response.AuthResponse;
import com.fitnessops.modules.auth.dto.response.SessionResponse;
import com.fitnessops.modules.auth.service.AuthService;
import com.fitnessops.modules.auth.service.SessionService;
import com.fitnessops.security.annotation.CurrentUser;
import com.fitnessops.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.AUTH)
@RequiredArgsConstructor
@Tag(name = "Đăng nhập", description = "NCL-01-CN-001 Đăng nhập hệ thống")
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng tên đăng nhập và mật khẩu",
            description = "Gửi kèm header X-Device-Token nếu máy là máy quầy đã đăng ký.")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, ClientRequestInfo.from(httpRequest));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất, kết thúc phiên hiện tại")
    public ResponseEntity<Void> logout(@CurrentUser CustomUserDetails principal, HttpServletRequest httpRequest) {
        sessionService.logout(principal, ClientRequestInfo.from(httpRequest));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Phiên hiện tại", description = "Đồng thời giữ phiên vì được tính là một lần thao tác.")
    public SessionResponse me(@CurrentUser CustomUserDetails principal) {
        return sessionService.describe(principal);
    }
}
