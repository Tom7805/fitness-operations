package com.fitnessops.support;

import com.fitnessops.modules.auth.service.DeviceTokens;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Tạo dữ liệu kiểm thử trực tiếp trong cơ sở dữ liệu. Mỗi lần gọi sinh tên duy nhất nên các kiểm thử không phụ
 * thuộc nhau và không cần dọn dữ liệu (nhật ký đăng nhập vốn không xóa được).
 */
@Component
public class TestDataFactory {

    public static final String PASSWORD = "Fitness@2026";

    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    /** Cost thấp để kiểm thử nhanh; BCrypt đọc cost từ chính bản băm khi so khớp. */
    private static final BCryptPasswordEncoder FAST_ENCODER = new BCryptPasswordEncoder(4);

    private final JdbcTemplate jdbc;

    public TestDataFactory(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public static String unique(String prefix) {
        return prefix + System.nanoTime() % 1_000_000 + SEQUENCE.incrementAndGet();
    }

    public long createBranch(String name) {
        String code = unique("CLB").toUpperCase(Locale.ROOT);
        String uniqueName = name + " " + code;
        return jdbc.queryForObject("insert into branches (code, name, status) values (?, ?, 'ACTIVE') returning id",
                Long.class, code, uniqueName);
    }

    public String branchName(long branchId) {
        return jdbc.queryForObject("select name from branches where id = ?", String.class, branchId);
    }

    public UserBuilder user(String usernamePrefix) {
        return new UserBuilder(unique(usernamePrefix));
    }

    /** Đăng ký sẵn một máy quầy, trả về mã máy quầy dạng rõ. */
    public String registerDevice(long branchId, String name, long registeredByUserId) {
        String token = DeviceTokens.generate();
        jdbc.update("insert into counter_devices (branch_id, name, token_hash, status, registered_by_user_id, "
                        + "registered_at) values (?, ?, ?, 'ACTIVE', ?, ?)",
                branchId, name, DeviceTokens.hash(token), registeredByUserId, Timestamp.from(Instant.now()));
        return token;
    }

    /** Dựng tài khoản kiểm thử. */
    public final class UserBuilder {

        private final String username;
        private String status = "ACTIVE";
        private boolean allBranches;
        private List<String> roles = List.of();
        private List<Long> branchIds = List.of();

        private UserBuilder(String username) {
            this.username = username;
        }

        public UserBuilder roles(String... roleCodes) {
            this.roles = List.of(roleCodes);
            return this;
        }

        public UserBuilder branches(Long... ids) {
            this.branchIds = List.of(ids);
            return this;
        }

        public UserBuilder allBranches() {
            this.allBranches = true;
            return this;
        }

        public UserBuilder disabled() {
            this.status = "DISABLED";
            return this;
        }

        public TestUser create() {
            Long id = jdbc.queryForObject("insert into users (username, password_hash, full_name, job_title, status, "
                            + "all_branches) values (?, ?, ?, ?, ?, ?) returning id", Long.class,
                    username, FAST_ENCODER.encode(PASSWORD), "Người dùng " + username, "Nhân viên", status,
                    allBranches);
            for (String role : roles) {
                jdbc.update("insert into user_roles (user_id, role_id) select ?, id from roles where code = ?",
                        id, role);
            }
            for (Long branchId : branchIds) {
                jdbc.update("insert into user_branch_scopes (user_id, branch_id) values (?, ?)", id, branchId);
            }
            return new TestUser(id, username);
        }
    }

    /** Tài khoản đã tạo; mật khẩu luôn là {@link #PASSWORD}. */
    public record TestUser(long id, String username) {
    }
}
