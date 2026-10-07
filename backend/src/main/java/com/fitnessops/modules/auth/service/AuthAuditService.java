package com.fitnessops.modules.auth.service;

/** Ghi nhật ký đăng nhập, chỉ thêm mới (QTN-02). */
public interface AuthAuditService {

    /**
     * Ghi một sự kiện trong giao dịch đang mở của thao tác: không ghi được nhật ký thì thao tác không hoàn tất.
     *
     * @throws org.springframework.transaction.IllegalTransactionStateException khi gọi ngoài giao dịch
     */
    void record(AuthAuditEvent event);
}
