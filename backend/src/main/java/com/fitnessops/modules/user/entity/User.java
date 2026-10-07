package com.fitnessops.modules.user.entity;

import com.fitnessops.modules.user.enums.UserStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Tài khoản nhân viên. Chứa trạng thái đăng nhập (bộ đếm sai liên tiếp, thời điểm hết khóa) để
 * quy tắc khóa được áp dụng ngay trong giao dịch đã khóa dòng tài khoản.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    /** Phạm vi toàn chuỗi; nếu {@code false} chỉ làm việc tại {@link #branchIds}. */
    @Column(name = "all_branches", nullable = false)
    private boolean allBranches;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    /** Câu lạc bộ được giao. Chỉ giữ mã để module tài khoản không phụ thuộc thực thể của module câu lạc bộ. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_branch_scopes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "branch_id", nullable = false)
    private Set<Long> branchIds = new HashSet<>();

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /** Đang trong thời gian tạm khóa đăng nhập. */
    public boolean isTemporarilyLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    /** Thời gian khóa đã qua thì bộ đếm về 0 để người dùng có lại đủ số lần thử. */
    public void clearExpiredLock(Instant now) {
        if (lockedUntil != null && !lockedUntil.isAfter(now)) {
            lockedUntil = null;
            failedLoginAttempts = 0;
            updatedAt = now;
        }
    }

    /**
     * Ghi nhận một lần nhập sai mật khẩu.
     *
     * @return {@code true} nếu lần sai này làm tài khoản bị tạm khóa
     */
    public boolean registerFailedLogin(int maxFailedAttempts, Duration lockDuration, Instant now) {
        failedLoginAttempts++;
        updatedAt = now;
        if (failedLoginAttempts >= maxFailedAttempts) {
            lockedUntil = now.plus(lockDuration);
            return true;
        }
        return false;
    }

    /** Mật khẩu đúng: các lần sai trước đó không còn "liên tiếp". */
    public void resetFailedLogins(Instant now) {
        if (failedLoginAttempts != 0 || lockedUntil != null) {
            failedLoginAttempts = 0;
            lockedUntil = null;
            updatedAt = now;
        }
    }

    public void recordSuccessfulLogin(Instant now) {
        resetFailedLogins(now);
        lastLoginAt = now;
        updatedAt = now;
    }

    public boolean requiresCounterDevice() {
        return roles.stream().anyMatch(Role::isRequiresCounterDevice);
    }

    public boolean canWorkAtBranch(Long branchId) {
        return allBranches || branchIds.contains(branchId);
    }

    public boolean hasRole(String roleCode) {
        return roles.stream().anyMatch(role -> role.getCode().equals(roleCode));
    }

    public List<Role> sortedRoles() {
        return roles.stream().sorted(Comparator.comparingInt(Role::getSortOrder)).toList();
    }
}
