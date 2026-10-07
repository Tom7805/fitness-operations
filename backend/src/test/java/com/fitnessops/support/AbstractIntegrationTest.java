package com.fitnessops.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitnessops.common.constant.SecurityConstants;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/** Nền cho kiểm thử tích hợp: toàn bộ ứng dụng, PostgreSQL thật, migration Flyway thật. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
public abstract class AbstractIntegrationTest {

    protected static final String LOGIN_URL = "/api/v1/auth/login";
    protected static final String ME_URL = "/api/v1/auth/me";
    protected static final String LOGOUT_URL = "/api/v1/auth/logout";
    protected static final String DEVICES_URL = "/api/v1/devices";
    protected static final String CURRENT_DEVICE_URL = "/api/v1/devices/current";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MutableClock clock;

    @Autowired
    protected TestDataFactory data;

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void resetClock() {
        clock.reset();
    }

    protected ResultActions login(String username, String password, String deviceToken) throws Exception {
        return login(username, password, deviceToken, null);
    }

    protected ResultActions login(String username, String password, String deviceToken, String clientType)
            throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("password", password);
        if (clientType != null) {
            body.put("clientType", clientType);
        }
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "JUnit")
                .content(objectMapper.writeValueAsString(body));
        if (deviceToken != null) {
            request.header(SecurityConstants.DEVICE_TOKEN_HEADER, deviceToken);
        }
        return mockMvc.perform(request);
    }

    protected MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request, String accessToken,
                                                       String deviceToken) {
        request.header(SecurityConstants.AUTHORIZATION_HEADER, SecurityConstants.BEARER_PREFIX + accessToken);
        if (deviceToken != null) {
            request.header(SecurityConstants.DEVICE_TOKEN_HEADER, deviceToken);
        }
        return request;
    }

    protected JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected String accessToken(MvcResult result) throws Exception {
        return json(result).get("accessToken").asText();
    }
}
