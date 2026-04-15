package com.example.app.domain.notification.adapter;

public interface EmailAdapter {

    void send(String to, String subject, String content);
}
