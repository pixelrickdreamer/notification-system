package com.example.notifications.routing.rules;

import com.example.notifications.Notification;
import com.example.notifications.model.IncomingEvent;
import com.example.notifications.model.Reaction;
import com.example.notifications.routing.RoutingRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class InventoryLowRule implements RoutingRule {

    @Override
    public boolean matches(IncomingEvent event) {
        return "inventory.low".equals(event.type());
    }

    @Override
    public List<Reaction> getReactions(IncomingEvent event) {
        String productId = event.getPayloadValue("productId");
        String productName = event.getPayloadValue("productName");
        Number currentStock = event.getPayloadNumber("currentStock");

        String displayName = productName != null ? productName : productId;

        Notification notification = Notification.create(
            "system",
            "warning",
            String.format("Low inventory alert: %s has only %d units left",
                displayName, currentStock != null ? currentStock.intValue() : 0)
        );

        Map<String, Object> alertPayload = Map.of(
            "type", "inventory_low",
            "productId", productId != null ? productId : "unknown",
            "currentStock", currentStock != null ? currentStock : 0
        );

        return List.of(
            new Reaction.PushToFrontend(notification),
            new Reaction.PublishToKafka("alerts", alertPayload),
            new Reaction.LogEvent("WARN", "Low inventory detected", event)
        );
    }
}
