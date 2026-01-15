package com.example.notifications.routing.rules;

import com.example.notifications.Notification;
import com.example.notifications.model.IncomingEvent;
import com.example.notifications.model.Reaction;
import com.example.notifications.routing.RoutingRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HighValueOrderRule implements RoutingRule {

    private static final double HIGH_VALUE_THRESHOLD = 1000.0;

    @Override
    public boolean matches(IncomingEvent event) {
        if (!"order.created".equals(event.type())) {
            return false;
        }

        Number amount = event.getPayloadNumber("amount");
        return amount != null && amount.doubleValue() > HIGH_VALUE_THRESHOLD;
    }

    @Override
    public List<Reaction> getReactions(IncomingEvent event) {
        Number amount = event.getPayloadNumber("amount");
        String orderId = event.getPayloadValue("orderId");

        Notification notification = Notification.create(
            "system",
            "warning",
            String.format("High-value order detected! Order %s for $%.2f", orderId, amount.doubleValue())
        );

        return List.of(
            new Reaction.PushToFrontend(notification),
            new Reaction.LogEvent("INFO", "High-value order processed", event)
        );
    }
}
