package com.fitnessops.modules.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitnessops.modules.auth.entity.AuthAuditLog;
import com.fitnessops.modules.auth.enums.AuthEventReason;
import com.fitnessops.modules.auth.enums.AuthEventType;
import com.fitnessops.modules.auth.enums.ClientType;
import com.fitnessops.modules.auth.repository.AuthAuditLogRepository;
import com.fitnessops.support.AbstractIntegrationTest;
import com.fitnessops.support.TestDataFactory;
import com.fitnessops.support.TestDataFactory.TestUser;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.web.servlet.MvcResult;

/** NCL-01-CN-001 Đăng nhập hệ thống — kiểm thử ở mức API và dữ liệu theo tiêu chí chấp nhận TC-01 … TC-04. */
@DisplayName("NCL-01-CN-001 Đăng nhập hệ thống")
class AuthIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = TestDataFactory.PASSWORD;
    private static final String WRONG_PASSWORD = "Sai@Mat-Khau";

    @Autowired
    private AuthAuditLogRepository auditLogRepository;

    @Nested
    @DisplayName("TC-01 Luồng thành công: lễ tân đăng nhập trên máy quầy đã đăng ký")
    class Tc01 {

        @Test
        @DisplayName("Mở trang chính theo vai trò lễ tân và ghi nhận phiên gắn với máy quầy")
        void tc01_receptionistOnRegisteredCounter_opensReceptionistSessionBoundToDevice() throws Exception {
            long branchId = data.createBranch("CLB Cầu Giấy");
            TestUser manager = data.user("quanly").roles("CLUB_MANAGER").branches(branchId).create();
            TestUser receptionist = data.user("letan").roles("RECEPTIONIST").branches(branchId).create();
            String deviceToken = data.registerDevice(branchId, "Quầy lễ tân 1", manager.id());

            MvcResult result = login(receptionist.username(), PASSWORD, deviceToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.session.clientType").value("COUNTER"))
                    .andExpect(jsonPath("$.session.idleTimeoutSeconds").value(1800))
                    .andExpect(jsonPath("$.session.user.username").value(receptionist.username()))
                    .andExpect(jsonPath("$.session.user.roles[0].code").value("RECEPTIONIST"))
                    .andExpect(jsonPath("$.session.user.roles[0].name").value("Lễ tân"))
                    .andExpect(jsonPath("$.session.user.branches[0].id").value(branchId))
                    .andExpect(jsonPath("$.session.activeBranch.id").value(branchId))
                    .andExpect(jsonPath("$.session.device.name").value("Quầy lễ tân 1"))
                    .andExpect(jsonPath("$.session.device.branch.id").value(branchId))
                    .andReturn();

            UUID sessionId = UUID.fromString(json(result).at("/session/sessionId").asText());
            Map<String, Object> row = jdbc.queryForMap(
                    "select s.user_id, s.client_type, s.branch_id, d.name as device_name "
                            + "from user_sessions s join counter_devices d on d.id = s.device_id where s.id = ?",
                    sessionId.toString());
            assertThat(row).containsEntry("user_id", receptionist.id())
                    .containsEntry("client_type", "COUNTER")
                    .containsEntry("branch_id", branchId)
                    .containsEntry("device_name", "Quầy lễ tân 1");

            mockMvc.perform(authorized(get(ME_URL), accessToken(result), deviceToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                    .andExpect(jsonPath("$.device.name").value("Quầy lễ tân 1"));
        }

        @Test
        @DisplayName("Mã phiên máy quầy mang sang máy khác bị từ chối")
        void counterSession_usedWithoutTheSameDevice_isRejected() throws Exception {
            long branchId = data.createBranch("CLB Hai Bà Trưng");
            TestUser manager = data.user("quanly").roles("CLUB_MANAGER").branches(branchId).create();
            TestUser receptionist = data.user("letan").roles("RECEPTIONIST").branches(branchId).create();
            String deviceToken = data.registerDevice(branchId, "Quầy 1", manager.id());
            String otherDevice = data.registerDevice(branchId, "Quầy 2", manager.id());
            String token = accessToken(login(receptionist.username(), PASSWORD, deviceToken).andReturn());

            mockMvc.perform(authorized(get(ME_URL), token, null))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("SESSION_INVALID"));
            mockMvc.perform(authorized(get(ME_URL), token, otherDevice))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("SESSION_INVALID"));
        }

        @Test
        @DisplayName("Huấn luyện viên đăng nhập trên điện thoại, không cần máy quầy")
        void trainerOnPhone_logsInWithoutCounterDevice() throws Exception {
            long branchId = data.createBranch("CLB Đống Đa");
            TestUser trainer = data.user("hlv").roles("PERSONAL_TRAINER").branches(branchId).create();

            login(trainer.username(), PASSWORD, null, "MOBILE")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.session.clientType").value("MOBILE"))
                    .andExpect(jsonPath("$.session.activeBranch.id").value(branchId))
                    .andExpect(jsonPath("$.session.device").doesNotExist());
        }

        @Test
        @DisplayName("Chủ phòng tập toàn chuỗi đăng nhập trên máy văn phòng, tên đăng nhập không phân biệt hoa thường")
        void chainOwnerOnOffice_hasChainWideScope() throws Exception {
            data.createBranch("CLB Tây Hồ");
            TestUser owner = data.user("chuphongtap").roles("CHAIN_OWNER").allBranches().create();

            login("  " + owner.username().toUpperCase() + " ", PASSWORD, null, "OFFICE")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.session.clientType").value("OFFICE"))
                    .andExpect(jsonPath("$.session.user.allBranches").value(true))
                    .andExpect(jsonPath("$.session.user.branches").isNotEmpty())
                    .andExpect(jsonPath("$.session.activeBranch").doesNotExist());
        }
    }

    @Nested
    @DisplayName("TC-02 Dữ liệu không hợp lệ: nhập sai mật khẩu năm lần liên tiếp")
    class Tc02 {

        @Test
        @DisplayName("Lần thứ sáu bị tạm khóa 15 phút và được báo thời gian chờ")
        void tc02_sixthAttemptAfterFiveWrongPasswords_isLockedFor15MinutesWithWaitTime() throws Exception {
            TestUser user = data.user("ketoan").roles("ACCOUNTANT").allBranches().create();

            for (int attempt = 1; attempt <= 4; attempt++) {
                login(user.username(), WRONG_PASSWORD, null)
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                        .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không đúng."));
            }
            login(user.username(), WRONG_PASSWORD, null)
                    .andExpect(status().isLocked())
                    .andExpect(jsonPath("$.code").value("ACCOUNT_TEMPORARILY_LOCKED"))
                    .andExpect(jsonPath("$.details.retryAfterSeconds").value(900));

            clock.advance(Duration.ofSeconds(30));
            // Lần thứ sáu — kể cả mật khẩu đúng — bị từ chối, kèm thời gian chờ còn lại.
            login(user.username(), PASSWORD, null)
                    .andExpect(status().isLocked())
                    .andExpect(header().string("Retry-After", "870"))
                    .andExpect(jsonPath("$.code").value("ACCOUNT_TEMPORARILY_LOCKED"))
                    .andExpect(jsonPath("$.message").value(
                            "Tài khoản tạm khóa do nhập sai mật khẩu 5 lần liên tiếp. Vui lòng thử lại sau 15 phút."))
                    .andExpect(jsonPath("$.details.retryAfterSeconds").value(870))
                    .andExpect(jsonPath("$.details.lockedUntil").isNotEmpty());

            Map<String, Object> state = jdbc.queryForMap(
                    "select failed_login_attempts, locked_until is not null as locked from users where id = ?",
                    user.id());
            assertThat(state).containsEntry("failed_login_attempts", 5)
                    .hasEntrySatisfying("locked", locked -> assertThat(((Number) locked).intValue()).isEqualTo(1));

            clock.advance(Duration.ofMinutes(14).plusSeconds(29));
            login(user.username(), PASSWORD, null)
                    .andExpect(status().isLocked())
                    .andExpect(jsonPath("$.details.retryAfterSeconds").value(1))
                    .andExpect(jsonPath("$.message").value(Matchers.endsWith("sau 1 phút.")));

            clock.advance(Duration.ofSeconds(1));
            login(user.username(), PASSWORD, null).andExpect(status().isOk());
            assertThat(jdbc.queryForObject("select failed_login_attempts from users where id = ?", Integer.class,
                    user.id())).isZero();
        }

        @Test
        @DisplayName("Hết thời gian khóa, người dùng có lại đủ 5 lần thử")
        void afterLockExpires_userGetsFreshAttempts() throws Exception {
            TestUser user = data.user("kythuat").roles("TECHNICIAN").allBranches().create();
            for (int i = 0; i < 5; i++) {
                login(user.username(), WRONG_PASSWORD, null);
            }
            clock.advance(Duration.ofMinutes(15));
            login(user.username(), WRONG_PASSWORD, null)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
            assertThat(jdbc.queryForObject("select failed_login_attempts from users where id = ?", Integer.class,
                    user.id())).isEqualTo(1);
        }

        @Test
        @DisplayName("Đăng nhập đúng xen giữa làm các lần sai không còn liên tiếp")
        void successfulLogin_resetsConsecutiveFailures() throws Exception {
            TestUser user = data.user("tuvan").roles("SALES_CONSULTANT").allBranches().create();
            for (int i = 0; i < 4; i++) {
                login(user.username(), WRONG_PASSWORD, null).andExpect(status().isUnauthorized());
            }
            login(user.username(), PASSWORD, null).andExpect(status().isOk());
            for (int i = 0; i < 4; i++) {
                login(user.username(), WRONG_PASSWORD, null).andExpect(status().isUnauthorized());
            }
            login(user.username(), PASSWORD, null).andExpect(status().isOk());
        }

        @Test
        @DisplayName("Tên đăng nhập không tồn tại nhận cùng thông báo với sai mật khẩu")
        void unknownUsername_getsSameMessageAsWrongPassword() throws Exception {
            login(TestDataFactory.unique("khongco"), PASSWORD, null)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                    .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không đúng."));
        }

        @Test
        @DisplayName("Thiếu tên đăng nhập hoặc mật khẩu bị từ chối và không tính là nhập sai")
        void blankCredentials_failValidation() throws Exception {
            login("", "", null)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors[*].field",
                            Matchers.containsInAnyOrder("username", "password")));
        }

        @Test
        @DisplayName("Tài khoản đã bị khóa chỉ bị báo sau khi mật khẩu đúng")
        void disabledAccount_isReportedOnlyAfterCorrectPassword() throws Exception {
            TestUser user = data.user("nghiviec").roles("TECHNICIAN").allBranches().disabled().create();
            login(user.username(), WRONG_PASSWORD, null)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
            login(user.username(), PASSWORD, null)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("ACCOUNT_DISABLED"));
        }
    }

    @Nested
    @DisplayName("TC-03 Ngoại lệ: máy tính bảng mới đặt tại quầy chưa được đăng ký")
    class Tc03 {

        @Test
        @DisplayName("Máy chưa đăng ký được báo chưa đăng ký khi mở màn hình đăng nhập")
        void tc03_unregisteredDevice_isReportedAsNotRegistered() throws Exception {
            mockMvc.perform(get(CURRENT_DEVICE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(false))
                    .andExpect(jsonPath("$.device").doesNotExist());
            mockMvc.perform(get(CURRENT_DEVICE_URL).header("X-Device-Token", "ma-may-quay-khong-ton-tai"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(false));
        }

        @Test
        @DisplayName("Lễ tân đăng nhập trên máy chưa đăng ký được yêu cầu nhờ quản lý đăng ký máy")
        void tc03_receptionistOnUnregisteredDevice_isAskedToHaveManagerRegisterIt() throws Exception {
            long branchId = data.createBranch("CLB Long Biên");
            TestUser receptionist = data.user("letan").roles("RECEPTIONIST").branches(branchId).create();

            login(receptionist.username(), PASSWORD, null)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("DEVICE_NOT_REGISTERED"))
                    .andExpect(jsonPath("$.message").value(Matchers.containsString("quản lý câu lạc bộ")));
            assertThat(jdbc.queryForObject("select failed_login_attempts from users where id = ?", Integer.class,
                    receptionist.id())).isZero();
        }

        @Test
        @DisplayName("Quản lý đăng ký máy với câu lạc bộ, sau đó lễ tân dùng được máy")
        void tc03_managerRegistersDevice_thenReceptionistCanUseIt() throws Exception {
            long branchId = data.createBranch("CLB Thanh Xuân");
            TestUser manager = data.user("quanly").roles("CLUB_MANAGER").branches(branchId).create();
            TestUser receptionist = data.user("letan").roles("RECEPTIONIST").branches(branchId).create();

            String managerToken = accessToken(login(manager.username(), PASSWORD, null, "MOBILE")
                    .andExpect(status().isOk()).andReturn());
            mockMvc.perform(authorized(get(DEVICES_URL + "/registrable-branches"), managerToken, null))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(branchId));

            MvcResult registration = mockMvc.perform(authorized(post(DEVICES_URL), managerToken, null)
                            .contentType("application/json")
                            .content("{\"branchId\": " + branchId + ", \"name\": \"  Quầy   lễ tân 1 \"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.device.name").value("Quầy lễ tân 1"))
                    .andExpect(jsonPath("$.device.branch.id").value(branchId))
                    .andExpect(jsonPath("$.deviceToken").isNotEmpty())
                    .andReturn();
            String deviceToken = json(registration).get("deviceToken").asText();

            assertThat(jdbc.queryForObject("select count(*) from counter_devices where token_hash = ?",
                    Integer.class, deviceToken)).as("mã máy quầy không được lưu dạng rõ").isZero();

            mockMvc.perform(get(CURRENT_DEVICE_URL).header("X-Device-Token", deviceToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(true))
                    .andExpect(jsonPath("$.device.name").value("Quầy lễ tân 1"));

            login(receptionist.username(), PASSWORD, deviceToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.session.clientType").value("COUNTER"));
        }

        @Test
        @DisplayName("Nhân viên được giao câu lạc bộ khác không đăng nhập được trên máy quầy này")
        void userNotAssignedToDeviceBranch_isRejected() throws Exception {
            long branchA = data.createBranch("CLB Hoàng Mai");
            long branchB = data.createBranch("CLB Hà Đông");
            TestUser manager = data.user("quanly").roles("CLUB_MANAGER").branches(branchA).create();
            TestUser receptionistB = data.user("letan").roles("RECEPTIONIST").branches(branchB).create();
            String deviceA = data.registerDevice(branchA, "Quầy 1", manager.id());

            login(receptionistB.username(), PASSWORD, deviceA)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("DEVICE_BRANCH_NOT_ASSIGNED"))
                    .andExpect(jsonPath("$.details.branchName").value(data.branchName(branchA)));
        }
    }

    @Nested
    @DisplayName("TC-04 Lưu lịch sử: ghi người thực hiện, nội dung và thời điểm")
    class Tc04 {

        @Test
        @DisplayName("Mọi lần đăng nhập thành công hoặc thất bại đều có nhật ký")
        void tc04_everyLoginAttempt_isLoggedWithActorContentAndTime() throws Exception {
            long branchId = data.createBranch("CLB Nam Từ Liêm");
            TestUser manager = data.user("quanly").roles("CLUB_MANAGER").branches(branchId).create();
            TestUser receptionist = data.user("letan").roles("RECEPTIONIST").branches(branchId).create();
            String deviceToken = data.registerDevice(branchId, "Quầy lễ tân 1", manager.id());

            login(receptionist.username(), WRONG_PASSWORD, deviceToken).andExpect(status().isUnauthorized());
            clock.advance(Duration.ofSeconds(5));
            MvcResult success = login(receptionist.username(), PASSWORD, deviceToken)
                    .andExpect(status().isOk()).andReturn();
            String sessionId = json(success).at("/session/sessionId").asText();

            List<AuthAuditLog> logs = auditLogRepository.findAllByUsernameOrderByIdAsc(receptionist.username());
            assertThat(logs).hasSize(2);

            AuthAuditLog failed = logs.get(0);
            assertThat(failed.getEventType()).isEqualTo(AuthEventType.LOGIN_FAILED);
            assertThat(failed.getReasonCode()).isEqualTo(AuthEventReason.INVALID_PASSWORD);
            assertThat(failed.getUserId()).isEqualTo(receptionist.id());
            assertThat(failed.getDetail()).isEqualTo("Đăng nhập thất bại: sai mật khẩu (lần 1/5 liên tiếp)");
            assertThat(failed.getOccurredAt()).isEqualTo(clock.instant().minusSeconds(5));
            assertThat(failed.getClientType()).isEqualTo(ClientType.COUNTER);
            assertThat(failed.getBranchId()).isEqualTo(branchId);
            assertThat(failed.getUserAgent()).isEqualTo("JUnit");
            assertThat(failed.getIpAddress()).isNotBlank();

            AuthAuditLog succeeded = logs.get(1);
            assertThat(succeeded.getEventType()).isEqualTo(AuthEventType.LOGIN_SUCCEEDED);
            assertThat(succeeded.getUserId()).isEqualTo(receptionist.id());
            assertThat(succeeded.getSessionId()).hasToString(sessionId);
            assertThat(succeeded.getDeviceId()).isNotNull();
            assertThat(succeeded.getDetail()).isEqualTo("Đăng nhập thành công trên máy quầy \"Quầy lễ tân 1\" ("
                    + data.branchName(branchId) + ")");
            assertThat(succeeded.getOccurredAt()).isEqualTo(clock.instant());
        }

        @Test
        @DisplayName("Tên đăng nhập không tồn tại, tạm khóa và từ chối khi đang khóa đều được ghi")
        void tc04_unknownUserLockAndRejection_areLogged() throws Exception {
            String unknown = TestDataFactory.unique("khongco");
            login(unknown, PASSWORD, null).andExpect(status().isUnauthorized());
            assertThat(auditLogRepository.findAllByUsernameOrderByIdAsc(unknown))
                    .singleElement()
                    .satisfies(log -> {
                        assertThat(log.getEventType()).isEqualTo(AuthEventType.LOGIN_FAILED);
                        assertThat(log.getReasonCode()).isEqualTo(AuthEventReason.UNKNOWN_USERNAME);
                        assertThat(log.getUserId()).isNull();
                        assertThat(log.getDetail()).isEqualTo("Đăng nhập thất bại: tên đăng nhập không tồn tại");
                    });

            TestUser user = data.user("kythuat").roles("TECHNICIAN").allBranches().create();
            for (int i = 0; i < 6; i++) {
                login(user.username(), WRONG_PASSWORD, null);
            }
            List<AuthAuditLog> logs = auditLogRepository.findAllByUsernameOrderByIdAsc(user.username());
            assertThat(logs).extracting(AuthAuditLog::getEventType).containsExactly(
                    AuthEventType.LOGIN_FAILED, AuthEventType.LOGIN_FAILED, AuthEventType.LOGIN_FAILED,
                    AuthEventType.LOGIN_FAILED, AuthEventType.LOGIN_FAILED,
                    AuthEventType.ACCOUNT_TEMPORARILY_LOCKED, AuthEventType.LOGIN_REJECTED);
            assertThat(logs.get(5).getDetail()).startsWith("Tạm khóa đăng nhập 15 phút đến ")
                    .endsWith(" sau 5 lần nhập sai mật khẩu liên tiếp");
            assertThat(logs.get(6).getReasonCode()).isEqualTo(AuthEventReason.ACCOUNT_LOCKED);
        }

        @Test
        @DisplayName("Nhật ký chỉ được thêm mới: sửa, xóa đều bị cơ sở dữ liệu từ chối (QTN-02)")
        void auditLog_isAppendOnly() throws Exception {
            String unknown = TestDataFactory.unique("khongco");
            login(unknown, PASSWORD, null).andExpect(status().isUnauthorized());
            Long id = auditLogRepository.findAllByUsernameOrderByIdAsc(unknown).get(0).getId();

            assertThatThrownBy(() -> jdbc.update("update auth_audit_logs set detail = 'sửa' where id = ?", id))
                    .isInstanceOf(DataAccessException.class)
                    .rootCause()
                    .hasMessageContaining("chỉ cho phép thêm mới");
            assertThatThrownBy(() -> jdbc.update("delete from auth_audit_logs where id = ?", id))
                    .isInstanceOf(DataAccessException.class)
                    .rootCause()
                    .hasMessageContaining("chỉ cho phép thêm mới");

        }
    }

    @Nested
    @DisplayName("Phiên làm việc: hết hạn sau 30 phút không thao tác, đăng xuất")
    class Session {

        @Test
        @DisplayName("Mỗi thao tác gia hạn phiên; 30 phút không thao tác thì phiên hết hạn và được ghi nhật ký")
        void sessionExpiresAfter30MinutesOfInactivity() throws Exception {
            TestUser user = data.user("chamsoc").roles("MEMBER_CARE").allBranches().create();
            String token = accessToken(login(user.username(), PASSWORD, null).andReturn());

            clock.advance(Duration.ofMinutes(29));
            mockMvc.perform(authorized(get(ME_URL), token, null)).andExpect(status().isOk());
            clock.advance(Duration.ofMinutes(29));
            mockMvc.perform(authorized(get(ME_URL), token, null)).andExpect(status().isOk());

            clock.advance(Duration.ofMinutes(30));
            mockMvc.perform(authorized(get(ME_URL), token, null))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("SESSION_EXPIRED"))
                    .andExpect(jsonPath("$.message").value(
                            "Phiên làm việc đã hết hạn do không thao tác trong 30 phút. Vui lòng đăng nhập lại."));
            mockMvc.perform(authorized(get(ME_URL), token, null))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("SESSION_INVALID"));

            Map<String, Object> last = jdbc.queryForMap(
                    "select event_type, reason_code from auth_audit_logs where user_id = ? order by id desc limit 1",
                    user.id());
            assertThat(last).containsEntry("event_type", "SESSION_EXPIRED").containsEntry("reason_code", "IDLE_TIMEOUT");
        }

        @Test
        @DisplayName("Đăng xuất kết thúc phiên ngay ở máy chủ")
        void logout_endsSessionImmediately() throws Exception {
            TestUser user = data.user("hlvnhom").roles("GROUP_TRAINER").allBranches().create();
            String token = accessToken(login(user.username(), PASSWORD, null, "MOBILE").andReturn());

            mockMvc.perform(authorized(post(LOGOUT_URL), token, null)).andExpect(status().isNoContent());
            mockMvc.perform(authorized(get(ME_URL), token, null))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("SESSION_INVALID"));
            assertThat(jdbc.queryForObject(
                    "select count(*) from auth_audit_logs where user_id = ? and event_type = 'LOGOUT'",
                    Integer.class, user.id())).isEqualTo(1);
        }

        @Test
        @DisplayName("Chức năng cần đăng nhập từ chối yêu cầu không có hoặc có mã truy cập giả")
        void protectedEndpoint_withoutOrWithForgedToken_isUnauthorized() throws Exception {
            mockMvc.perform(get(ME_URL))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
            mockMvc.perform(authorized(get(ME_URL), "eyJhbGciOiJIUzI1NiJ9.e30.gia-mao", null))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("SESSION_INVALID"));
        }

        @Test
        @DisplayName("Mã truy cập cũ gửi kèm yêu cầu đăng nhập không cản trở đăng nhập")
        void staleTokenOnLogin_doesNotBlockLogin() throws Exception {
            TestUser user = data.user("ketoan").roles("ACCOUNTANT").allBranches().create();
            mockMvc.perform(post(LOGIN_URL)
                            .header("Authorization", "Bearer het-han")
                            .contentType("application/json")
                            .content("{\"username\":\"" + user.username() + "\",\"password\":\"" + PASSWORD + "\"}"))
                    .andExpect(status().isOk());
        }
    }
}
