package com.example.app.domain.auth;

import com.example.app.domain.auth.controller.AuthController;
import com.example.app.domain.auth.dto.LoginRequest;
import com.example.app.domain.auth.dto.LoginResponse;
import com.example.app.domain.auth.dto.UserDto;
import com.example.app.domain.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("올바른 이메일/비밀번호로 로그인 성공")
    void login_success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        UserDto user = new UserDto("uuid-1", "test@example.com", "홍길동", "USER", null);
        LoginResponse response = new LoginResponse("access-token", "Bearer", 900L, user);

        given(authService.login(any(), any())).willReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
    }

    @Test
    @DisplayName("이메일 형식 오류 시 400 반환")
    void login_invalid_email_returns_400() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("not-an-email", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
