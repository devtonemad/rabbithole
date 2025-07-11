package de.gnubis.rabbithole;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BlinkerController {

    @GetMapping("/blinker")
    public String showBlinker() {
        return "blinker"; // Will render src/main/resources/templates/blinker.html
    }
}