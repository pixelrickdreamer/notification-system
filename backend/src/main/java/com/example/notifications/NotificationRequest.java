package com.example.notifications;

public record NotificationRequest(
    String userId,
    String type,
    String message
) {}
