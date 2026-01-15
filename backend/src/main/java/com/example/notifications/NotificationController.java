package com.example.notifications;

import com.example.notifications.routing.ReactionExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final KafkaProducerService producerService;
    private final KafkaConsumerService consumerService;
    private final ReactionExecutor reactionExecutor;

    public NotificationController(
            KafkaProducerService producerService,
            KafkaConsumerService consumerService,
            ReactionExecutor reactionExecutor) {
        this.producerService = producerService;
        this.consumerService = consumerService;
        this.reactionExecutor = reactionExecutor;
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody NotificationRequest request) {
        Notification notification = Notification.create(
            request.userId(),
            request.type(),
            request.message()
        );
        producerService.sendNotification(notification);
        return ResponseEntity.ok(notification);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(consumerService.getAllNotifications());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Send existing notifications first
        consumerService.getAllNotifications().forEach(notification -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });

        // Listener for notifications from the original flow
        java.util.function.Consumer<Notification> consumerListener = notification -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        };

        // Listener for notifications from the EventRouter
        java.util.function.Consumer<Notification> routerListener = notification -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        };

        consumerService.addListener(consumerListener);
        reactionExecutor.addFrontendListener(routerListener);

        Runnable cleanup = () -> {
            consumerService.removeListener(consumerListener);
            reactionExecutor.removeFrontendListener(routerListener);
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }
}
