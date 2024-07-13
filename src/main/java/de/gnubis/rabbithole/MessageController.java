package de.gnubis.rabbithole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private RabbitMQReceiver rabbitMQReceiver;

    @GetMapping("/send")
    public String sendPage() {
        return "send";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam("destinationType") String destinationType,
                              @RequestParam("destination") String destination,
                              @RequestParam("routingKey") String routingKey,
                              @RequestParam("message") String message,
                              @RequestParam("headers") String headers,
                              Model model) {
        Map<String, Object> headersMap = new HashMap<>();
        if (!headers.isEmpty()) {
            String[] headersArray = headers.split(",");
            for (String header : headersArray) {
                String[] keyValue = header.split(":");
                if (keyValue.length == 2) {
                    headersMap.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }

        if (destinationType.equals("queue")) {
            rabbitMQSender.sendToQueue(destination, message);
        } else if (destinationType.equals("exchange")) {
            rabbitMQSender.sendToExchange(destination, routingKey, message, headersMap);
        } else if (destinationType.equals("stream")) {
            rabbitMQSender.sendToStream(destination, message);
        }

        // Pass the selected values back to the view
        model.addAttribute("destinationType", destinationType);
        model.addAttribute("destination", destination);
        model.addAttribute("routingKey", routingKey);
        model.addAttribute("headers", headers);
        model.addAttribute("message", message);

        return "send";
    }

    @GetMapping("/receive")
    public String receivePage(Model model) {
        model.addAttribute("messages", rabbitMQReceiver.getMessages());
        return "receive";
    }
}