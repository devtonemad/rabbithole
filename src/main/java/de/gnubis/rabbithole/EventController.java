package de.gnubis.rabbithole;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class EventController {

    @GetMapping("/events")
    public SseEmitter streamEvents() {
        SseEmitter emitter = new SseEmitter();
        // In a real scenario, you may use an asynchronous thread or scheduled tasks
        new Thread(() -> {
            try {
                while (true) {
                    // For demonstration, alternate events every second
                    emitter.send("on");
                    Thread.sleep(1000);
                    emitter.send("off");
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }
}