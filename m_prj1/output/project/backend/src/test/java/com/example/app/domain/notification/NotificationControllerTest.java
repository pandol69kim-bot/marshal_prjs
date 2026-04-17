package com.example.app.domain.notification;

import com.example.app.domain.auth.entity.User;
import com.example.app.domain.notification.controller.NotificationController;
import com.example.app.domain.notification.entity.NotificationHistory;
import com.example.app.domain.notification.repository.NotificationHistoryRepository;
import com.example.app.domain.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationHistoryRepository historyRepository;

    @Test
    @DisplayName("알림 이력 조회는 페이지 파라미터와 무관하게 전체 목록을 반환한다")
    @WithMockUser(roles = "ADMIN")
    void getList_returns_all_notifications_ignoring_pagination() throws Exception {
        NotificationHistory first = NotificationHistory.create(
                User.createLocal("first@example.com", "encoded", "첫번째"),
                "EMAIL",
                "제목1",
                "내용1");
        NotificationHistory second = NotificationHistory.create(
                User.createLocal("second@example.com", "encoded", "두번째"),
                "SMS",
                "제목2",
                "내용2");

        ReflectionTestUtils.setField(first, "id", 1L);
        ReflectionTestUtils.setField(second, "id", 2L);

        given(historyRepository.findAll(any(Sort.class))).willReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "1")
                        .param("size", "3")
                        .param("sort", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.meta.totalElements").value(2))
                .andExpect(jsonPath("$.meta.totalPages").value(1))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.meta.size").value(2));

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(historyRepository).findAll(sortCaptor.capture());
        assertThat(sortCaptor.getValue()).isEqualTo(Sort.by(Sort.Direction.ASC, "createdAt"));
    }
}