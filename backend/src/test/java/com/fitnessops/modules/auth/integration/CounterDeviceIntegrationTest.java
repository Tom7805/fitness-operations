package com.fitnessops.modules.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitnessops.support.AbstractIntegrationTest;
import com.fitnessops.support.TestDataFactory;
import com.fitnessops.support.TestDataFactory.TestUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

/** Đăng ký máy quầy với câu lạc bộ — quyền, phạm vi, trùng tên, đăng ký lại (R4). */
@DisplayName("NCL-01-CN-001 Đăng ký máy quầy")
class CounterDeviceIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = TestDataFactory.PASSWORD;

    @Test
    @DisplayName("Quản lý không đăng ký được máy cho câu lạc bộ ngoài phạm vi được giao")
    void managerOutsideBranchScope_isForbidden() throws Exception {
        long own = data.createBranch("CLB Cầu Giấy");
        long other = data.createBranch("CLB Tây Hồ");
        TestUser manager = data.user("quanly").roles("CLUB_MANAGER").branches(own).create();
        String token = accessToken(login(manager.username(), PASSWORD, null).andReturn());

        register(token, null, other, "Quầy 1")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("BRANCH_NOT_IN_SCOPE"));
    }

    @Test
    @DisplayName("Quản trị viên đăng ký được máy cho mọi câu lạc bộ")
    void admin_canRegisterForAnyBranch() throws Exception {
        long branch = data.createBranch("CLB Ba Đình");
        TestUser admin = data.user("quantri").roles("ADMIN").create();
        String token = accessToken(login(admin.username(), PASSWORD, null).andReturn());

        register(token, null, branch, "Quầy 1").andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Lễ tân không có quyền đăng ký máy quầy")
    void receptionist_cannotRegisterDevice() throws Exception {
        long branch = data.createBranch("CLB Đống Đa");
        TestUser manager = data.user("quanly").roles("CLUB_MANAGER").branches(branch).create();
        TestUser receptionist = data.user("letan").roles("RECEPTIONIST").branches(branch).create();
        String device = data.registerDevice(branch, "Quầy 1", manager.id());
        String token = accessToken(login(receptionist.username(), PASSWORD, device).andReturn());

        register(token, device, branch, "Quầy 2")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mockMvc.perform(authorized(get(DEVICES_URL + "/registrable-branches"), token, device))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tên máy trùng trong cùng câu lạc bộ (không phân biệt hoa thường) bị từ chối")
    void duplicateNameInBranch_isConflict() throws Exception {
        long branch = data.createBranch("CLB Hoàn Kiếm");
        TestUser manager = data.user("quanly").roles("CLUB_MANAGER").branches(branch).create();
        String token = accessToken(login(manager.username(), PASSWORD, null).andReturn());

        register(token, null, branch, "Quầy lễ tân 1").andExpect(status().isCreated());
        register(token, null, branch, "quầy LỄ TÂN 1")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEVICE_NAME_DUPLICATE"));
    }

    @Test
    @DisplayName("Câu lạc bộ không tồn tại và dữ liệu thiếu bị từ chối")
    void unknownBranchAndMissingFields_areRejected() throws Exception {
        TestUser admin = data.user("quantri").roles("ADMIN").create();
        String token = accessToken(login(admin.username(), PASSWORD, null).andReturn());

        register(token, null, 999_999_999L, "Quầy 1")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"));
        mockMvc.perform(authorized(post(DEVICES_URL), token, null)
                        .contentType("application/json").content("{\"name\": \" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Đăng ký lại một máy quầy thu hồi mã cũ của máy")
    void reRegisteringDevice_revokesPreviousToken() throws Exception {
        long branch = data.createBranch("CLB Gia Lâm");
        TestUser manager = data.user("quanly").roles("CLUB_MANAGER").branches(branch).create();
        String oldDevice = data.registerDevice(branch, "Quầy cũ", manager.id());
        String token = accessToken(login(manager.username(), PASSWORD, oldDevice).andReturn());

        MvcResult result = register(token, oldDevice, branch, "Quầy mới").andExpect(status().isCreated()).andReturn();
        String newDevice = json(result).get("deviceToken").asText();

        assertThat(newDevice).isNotEqualTo(oldDevice);
        mockMvc.perform(get(CURRENT_DEVICE_URL).header("X-Device-Token", oldDevice))
                .andExpect(jsonPath("$.registered").value(false));
        mockMvc.perform(get(CURRENT_DEVICE_URL).header("X-Device-Token", newDevice))
                .andExpect(jsonPath("$.registered").value(true))
                .andExpect(jsonPath("$.device.name").value("Quầy mới"));
        assertThat(jdbc.queryForObject("select count(*) from auth_audit_logs where user_id = ? "
                + "and event_type = 'DEVICE_REGISTERED'", Integer.class, manager.id())).isEqualTo(1);
    }

    private ResultActions register(String accessToken, String deviceToken, long branchId, String name)
            throws Exception {
        return mockMvc.perform(authorized(post(DEVICES_URL), accessToken, deviceToken)
                .contentType("application/json")
                .content("{\"branchId\": " + branchId + ", \"name\": \"" + name + "\"}"));
    }
}
