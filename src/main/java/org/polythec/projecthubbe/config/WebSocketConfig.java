package org.polythec.projecthubbe.config;

import org.polythec.projecthubbe.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.SimpleMessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    public WebSocketConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                                   org.springframework.http.server.ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        System.out.println("Performing handshake for: " + request.getURI());
                        return true; // Always allow handshake
                    }

                    @Override
                    public void afterHandshake(org.springframework.http.server.ServerHttpRequest request,
                                               org.springframework.http.server.ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                        // Nothing to do after handshake
                    }
                })
                .withSockJS()
                .setWebSocketEnabled(true)
                .setHeartbeatTime(25000)
                .setDisconnectDelay(5000)
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
                .setSessionCookieNeeded(false);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(128 * 1024) // 128KB
                .setSendBufferSizeLimit(512 * 1024) // 512KB
                .setSendTimeLimit(20000) // 20 seconds
                .addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
                    @Override
                    public WebSocketHandler decorate(final WebSocketHandler handler) {
                        return new WebSocketHandlerDecorator(handler) {
                            @Override
                            public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
                                System.out.println("WebSocket connection established: " + session.getId());
                                super.afterConnectionEstablished(session);
                            }

                            @Override
                            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)
                                    throws Exception {
                                System.out.println("WebSocket connection closed: " + session.getId() +
                                        " with status " + closeStatus);
                                super.afterConnectionClosed(session, closeStatus);
                            }
                        };
                    }
                });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[] {10000, 10000})
                .setTaskScheduler(heartbeatScheduler());
        config.setApplicationDestinationPrefixes("/app");
        config.setPreservePublishOrder(true);
    }

    @Bean
    public TaskScheduler heartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        return scheduler;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    try {
                        // Extract token from multiple possible sources
                        String token = extractToken(accessor);
                        System.out.println("WebSocket connection with token: " +
                                (token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null"));

                        // Only validate and set authentication if token is present
                        if (StringUtils.hasText(token)) {
                            if (jwtUtil.isTokenValid(token)) {
                                Authentication authentication = jwtUtil.getAuthentication(token);
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                accessor.setUser(authentication);
                                System.out.println("WebSocket authentication successful");
                            } else {
                                System.err.println("Invalid JWT token provided for WebSocket connection");
                            }
                        } else {
                            System.err.println("No JWT token provided for WebSocket connection");
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing WebSocket connection: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                return message;
            }

            /**
             * Extract JWT token from various possible sources in the request
             */
            private String extractToken(StompHeaderAccessor accessor) {
                // 1. Try to get from native token header
                String token = accessor.getFirstNativeHeader("token");
                if (StringUtils.hasText(token)) {
                    return token;
                }

                // 2. Try to get from Authorization header
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7).trim();
                }

                // 3. Try to get from session attributes
                if (accessor.getSessionAttributes() != null) {
                    Object tokenAttr = accessor.getSessionAttributes().get("token");
                    if (tokenAttr instanceof String) {
                        return (String) tokenAttr;
                    }
                }

                return null;
            }
        });
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        messageConverters.add(new SimpleMessageConverter());
        return false;
    }
}