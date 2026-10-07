package com.fitnessops.modules.auth.repository;

import com.fitnessops.modules.auth.entity.AuthAuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Chỉ dùng để thêm mới và đọc; mọi thao tác sửa xóa bị cơ sở dữ liệu từ chối. */
public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, Long> {

    List<AuthAuditLog> findAllByUsernameOrderByIdAsc(String username);
}
