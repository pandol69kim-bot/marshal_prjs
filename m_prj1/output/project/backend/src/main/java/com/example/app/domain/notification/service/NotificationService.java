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
        User user = userRepository.findById(UUID.fromString(userId)).orElse(null);
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
        User user = userRepository.findById(UUID.fromString(userId)).orElse(null);
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
}
