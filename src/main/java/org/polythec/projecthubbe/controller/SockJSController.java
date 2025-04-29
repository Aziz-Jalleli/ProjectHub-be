package org.polythec.projecthubbe.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller to handle SockJS info endpoint manually.
 * This ensures the /ws/info endpoint responds correctly to GET requests.
 */
@RestController
@RequestMapping("/ws")
public class SockJSController {

    /**
     * Manually handle the SockJS info endpoint request.
     * SockJS requires this endpoint to return specific information.
     *
     * @return SockJS server information JSON response
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> handleSockJsInfo() {
        Map<String, Object> info = new HashMap<>();

        // Standard SockJS info response fields
        info.put("websocket", true);
        info.put("cookie_needed", false);
        info.put("origins", new String[]{"*"});

        // Add entropy to prevent caching
        info.put("entropy", Math.round(Math.random() * 1000000));

        return new ResponseEntity<>(info, HttpStatus.OK);
    }
}
