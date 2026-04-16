package com.example.app.domain.notification.service;

import com.example.app.domain.auth.entity.User;
import com.example.app.domain.auth.repository.UserRepository;
import com.example.app.domain.notification.adapter.EmailAdapter;
import com.example.app.domain.notification.adapter.SmsAdapter;
import com.example.app.domain.notification.entity.NotificationHistory;
import com.example.app.domain.notification.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailAdapter emailAdapter;
    private final SmsAdapter smsAdapter;
    private final NotificationHistoryRepository historyRepository;
    private final UserRepository userRepository;

    @Async("notificationExecutor")
    public void sendEmail(String userId, String subject, String content) {
        User user = findUser(userId);
        if (user == null) {
            log.warn("이메일 발송 대상 사용자 없음: {}", userId);
            return;
        }

        NotificationHistory history = NotificationHistory.create(user, "EMAIL", subject, content);
        historyRepository.save(history);

        try {
            emailAdapter.send(user.getEmail(), subject, content);
            history.markSent();
        } catch (Exception e) {
            log.error("이메일 발송 실패: to={}, error={}", user.getEmail(), e.getMessage());
            history.markFailed(e.getMessage());
        } finally {
            historyRepository.save(history);
        }
    }

    @Async("notificationExecutor")
    public void sendSms(String userId, String phone, String message) {
        User user = findUser(userId);
        if (user == null) {
            log.warn("SMS 발송 대상 사용자 없음: {}", userId);
            return;
        }

        NotificationHistory history = NotificationHistory.create(user, "SMS", "SMS", message);
        historyRepository.save(history);

        try {
            smsAdapter.send(phone, message);
            history.markSent();
        } catch (Exception e) {
            log.error("SMS 발송 실패: phone={}, error={}", phone, e.getMessage());
            history.markFailed(e.getMessage());
        } finally {
            historyRepository.save(history);
        }
    }

    /**
     * userId 또는 email 어느 형식이든 사용자를 조회한다.
     * - "@" 포함 → 이메일로 조회
     * - 그 외 → UUID로 조회 (파싱 실패 시 null 반환)
     */
    private User findUser(String userIdOrEmail) {
        if (userIdOrEmail != null && userIdOrEmail.contains("@")) {
            return userRepository.findByEmail(userIdOrEmail).orElse(null);
        }
        try {
            return userRepository.findById(UUID.fromString(userIdOrEmail)).orElse(null);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 사용자 식별자 형식: {}", userIdOrEmail);
            return null;
        }
    }
}
