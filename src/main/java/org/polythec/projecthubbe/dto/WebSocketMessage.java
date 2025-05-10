package org.polythec.projecthubbe.dto;

/**
 * Generic wrapper for WebSocket messages with type and payload
 * @param <T> Type of the payload
 */
public class WebSocketMessage<T> {
    private String type;
    private T payload;

    public WebSocketMessage() {
    }

    public WebSocketMessage(String type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}