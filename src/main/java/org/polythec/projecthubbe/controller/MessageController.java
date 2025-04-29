package org.polythec.projecthubbe.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    @MessageMapping("/sendMessage") // client -> server
    @SendTo("/topic/messages")      // server -> clients
    public String broadcastMessage(String message) {
        return message; // echo message
    }
}

