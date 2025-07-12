package de.gnubis.rabbithole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private RabbitMQReceiver rabbitMQReceiver;

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/send")
    public String sendPage(Model model) {
        return "send";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam("destinationType") String destinationType,
                              @RequestParam("destination") String destination,
                              @RequestParam("routingKey") String routingKey,
                              @RequestParam("message") String message,
                              @RequestParam("headers") String headers,
                              RedirectAttributes redirectAttributes) {
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

        // Add attributes to the redirect
        redirectAttributes.addFlashAttribute("destinationType", destinationType);
        redirectAttributes.addFlashAttribute("destination", destination);
        redirectAttributes.addFlashAttribute("routingKey", routingKey);
        redirectAttributes.addFlashAttribute("headers", headers);
        redirectAttributes.addFlashAttribute("message", message);

        return "redirect:/send";
    }

    @GetMapping("/receive")
    public String receivePage(Model model) {
        return "receive";
    }

    @PostMapping("/receive")
    public String readMessages(@RequestParam("sourceType") String sourceType,
                               @RequestParam("source") String source,
                               @RequestParam(name = "startOffset", required = false) String startOffset,
                               Model model) {
        List<String> messages = rabbitMQReceiver.receiveMessages(sourceType, source, startOffset);
        model.addAttribute("messages", messages);
        model.addAttribute("sourceType", sourceType);
        model.addAttribute("source", source);
        model.addAttribute("startOffset", startOffset);
        return "receive";
    }
}