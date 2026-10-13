package mx.turnopro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration @EnableWebSocketMessageBroker public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final String origins;
    public WebSocketConfig(@org.springframework.beans.factory.annotation.Value("${app.cors.origins}") String origins){this.origins=origins;}
    public void configureMessageBroker(MessageBrokerRegistry r){r.enableSimpleBroker("/topic");r.setApplicationDestinationPrefixes("/app");}
    public void registerStompEndpoints(StompEndpointRegistry r){r.addEndpoint("/ws").setAllowedOrigins(origins.split(",")).withSockJS();}
}
