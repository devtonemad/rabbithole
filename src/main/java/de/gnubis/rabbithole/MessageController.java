package de.gnubis.rabbithole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String sendMessage(@RequestParam("queueName") String queueName, @RequestParam("message") String message) {
        rabbitMQSender.send(queueName, message);
        return "redirect:/send";
    }

    @GetMapping("/receive")
    public String receivePage(Model model) {
        model.addAttribute("messages", rabbitMQReceiver.getMessages());
        return "receive";
    }
}