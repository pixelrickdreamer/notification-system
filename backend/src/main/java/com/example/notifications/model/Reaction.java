package com.example.notifications.model;

import com.example.notifications.Notification;

public sealed interface Reaction {

    record PublishToKafka(String topic, Object message) implements Reaction {}

    record PushToFrontend(Notification notification) implements Reaction {}

    record CallExternalApi(String url, String method, Object body) implements Reaction {}

    record LogEvent(String level, String message, IncomingEvent event) implements Reaction {}
}
