package de.gnubis.rabbithole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/script")
public class ScriptController {

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @PostMapping("/execute")
    public ResponseEntity<String> execute(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> entries = (List<Map<String, String>>) body.get("entries");
            long pauseMs = body.get("pauseMs") instanceof Number n ? n.longValue() : 0L;

            for (int i = 0; i < entries.size(); i++) {
                Map<String, String> entry = entries.get(i);
                String type = entry.getOrDefault("destinationType", "queue");
                String destination = entry.getOrDefault("destination", "");
                String routingKey = entry.getOrDefault("routingKey", "");
                String headersRaw = entry.getOrDefault("headers", "");
                String message = entry.getOrDefault("message", "");

                Map<String, Object> headers = new HashMap<>();
                if (!headersRaw.isBlank()) {
                    for (String part : headersRaw.split("[,\\s]+")) {
                        String[] kv = part.split(":");
                        if (kv.length == 2) headers.put(kv[0].trim(), kv[1].trim());
                    }
                }

                switch (type) {
                    case "queue"    -> rabbitMQSender.sendToQueue(destination, message);
                    case "exchange" -> rabbitMQSender.sendToExchange(destination, routingKey, message, headers);
                    case "stream"   -> rabbitMQSender.sendToStream(destination, message, headers);
                }

                if (i < entries.size() - 1 && pauseMs > 0) {
                    Thread.sleep(pauseMs);
                }
            }

            return ResponseEntity.ok("Executed " + entries.size() + " message(s)");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

}
