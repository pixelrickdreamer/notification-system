package com.example.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * CLI tool to publish test events to Kafka topics.
 *
 * Usage: java EventProducerCli <topic> <event-type> <json-payload>
 *
 * Examples:
 *   java EventProducerCli orders.events order.created '{"orderId":"123","amount":1500}'
 *   java EventProducerCli payments.events payment.failed '{"paymentId":"456","reason":"Insufficient funds"}'
 *   java EventProducerCli inventory.events inventory.low '{"productId":"789","productName":"Widget","currentStock":5}'
 */
public class EventProducerCli {

    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }

        String topic = args[0];
        String eventType = args[1];
        String jsonPayload = args.length > 2 ? args[2] : "{}";

        try {
            Map<String, Object> payload = parsePayload(jsonPayload, eventType);
            sendEvent(topic, payload);
            System.out.println("Event sent successfully!");
            System.out.println("  Topic: " + topic);
            System.out.println("  Type: " + eventType);
            System.out.println("  Payload: " + payload);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Map<String, Object> parsePayload(String json, String eventType) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> payload = new HashMap<>();

        if (json != null && !json.equals("{}")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(json, Map.class);
            payload.putAll(parsed);
        }

        // Add standard fields
        payload.put("type", eventType);
        payload.put("source", "cli-producer");
        payload.put("eventId", UUID.randomUUID().toString());

        return payload;
    }

    private static void sendEvent(String topic, Map<String, Object> payload) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        try (KafkaProducer<String, Map<String, Object>> producer = new KafkaProducer<>(props)) {
            String key = UUID.randomUUID().toString();
            ProducerRecord<String, Map<String, Object>> record = new ProducerRecord<>(topic, key, payload);
            producer.send(record).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send event: " + e.getMessage(), e);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java EventProducerCli <topic> <event-type> [json-payload]");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  # High-value order (triggers HighValueOrderRule)");
        System.out.println("  java EventProducerCli orders.events order.created '{\"orderId\":\"123\",\"amount\":1500}'");
        System.out.println();
        System.out.println("  # Payment failure (triggers PaymentFailedRule)");
        System.out.println("  java EventProducerCli payments.events payment.failed '{\"paymentId\":\"456\",\"reason\":\"Insufficient funds\"}'");
        System.out.println();
        System.out.println("  # Low inventory (triggers InventoryLowRule)");
        System.out.println("  java EventProducerCli inventory.events inventory.low '{\"productId\":\"789\",\"productName\":\"Widget\",\"currentStock\":5}'");
    }
}
