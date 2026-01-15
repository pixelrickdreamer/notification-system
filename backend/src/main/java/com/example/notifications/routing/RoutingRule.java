package com.example.notifications.routing;

import com.example.notifications.model.IncomingEvent;
import com.example.notifications.model.Reaction;

import java.util.List;

public interface RoutingRule {

    /**
     * Check if this rule matches the incoming event.
     */
    boolean matches(IncomingEvent event);

    /**
     * Get the reactions to execute when this rule matches.
     */
    List<Reaction> getReactions(IncomingEvent event);

    /**
     * Rule name for logging purposes.
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
