package de.gnubis.rabbithole;
import org.springframework.beans.factory.annotation.Autowired; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/connection") public class RabbitMQConnectionController {
    @Autowired
    private RabbitMQConnectionService connectionService;

    @PostMapping("/connect")
    public ResponseEntity<String> connect(@RequestBody Map<String, String> params) {
        String host = params.get("host");
        int port = Integer.parseInt(params.get("port"));
        String username = params.get("username");
        String password = params.get("password");

        try {
            connectionService.connect(host, port, username, password);
            return ResponseEntity.ok("Connected successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Connection failed: " + e.getMessage());
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<String> disconnect() {
        connectionService.disconnect();
        return ResponseEntity.ok("Disconnected successfully");
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return connectionService.isConnected()
                ? ResponseEntity.ok("Connected")
                : ResponseEntity.ok("Disconnected");
    }

}
