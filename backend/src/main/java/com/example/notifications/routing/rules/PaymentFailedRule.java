package com.example.notifications.routing.rules;

import com.example.notifications.Notification;
import com.example.notifications.model.IncomingEvent;
import com.example.notifications.model.Reaction;
import com.example.notifications.routing.RoutingRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PaymentFailedRule implements RoutingRule {

    @Override
    public boolean matches(IncomingEvent event) {
        return "payment.failed".equals(event.type());
    }

    @Override
    public List<Reaction> getReactions(IncomingEvent event) {
        String paymentId = event.getPayloadValue("paymentId");
        String reason = event.getPayloadValue("reason");

        Notification notification = Notification.create(
            "system",
            "error",
            String.format("Payment %s failed: %s", paymentId, reason != null ? reason : "Unknown reason")
        );

        // Publish alert to alerts topic for other services to consume
        Map<String, Object> alertPayload = Map.of(
            "type", "payment_failure",
            "paymentId", paymentId != null ? paymentId : "unknown",
            "originalEvent", event.id()
        );

        return List.of(
            new Reaction.PushToFrontend(notification),
            new Reaction.PublishToKafka("alerts", alertPayload),
            new Reaction.LogEvent("ERROR", "Payment failure detected", event)
        );
    }
}
