package it.uniud.rmd.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocket implements WebSocketMessageBrokerConfigurer {

    /*  LINKS:
        endpoint: "/wss"
        AppPrefix: "/app"
        Broker: "/uniud"
    */

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //Endpoint della connessione
        registry.addEndpoint("/wss").withSockJS();
    }

    //Definiamo un canale ws
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/uniud");
    }

}