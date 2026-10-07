package com.fitnessops.modules.user.service;

import com.fitnessops.modules.user.entity.User;
import java.util.Optional;

/** Cổng truy cập tài khoản nhân viên cho các module khác. */
public interface UserService {

    /**
     * Tìm tài khoản theo tên đăng nhập (đã chuẩn hóa chữ thường) và khóa dòng tới hết giao dịch hiện tại.
     * Phải gọi trong một giao dịch đang mở.
     */
    Optional<User> findByUsernameForUpdate(String normalizedUsername);

    /** Tài khoản kèm vai trò và phạm vi câu lạc bộ. */
    Optional<User> findWithAccessById(Long id);
}
