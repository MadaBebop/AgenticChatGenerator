package it.uniud.rmd.Config;

import it.uniud.rmd.Chat.ChatMessage;
import it.uniud.rmd.Chat.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EventListenerConfig {

    private final SimpMessageSendingOperations messageSendingOperation;

    /*
        Questo metodo-listener informa quando uno user esce dalla chat testuale.

     */
    @EventListener
    public void disconnectListeners(SessionDisconnectEvent disconnectEvent) {

        //Lo stompHeaderAccessor prenderà le informazioni riguardo a chi si è disconnesso
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(disconnectEvent.getMessage());
        //Username disconnesso, -> throws NullPointerException
        String username = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");

        if(username != null){
            System.out.println("User disconnected");
            //Creaimo il messaggio di disconnessione dell'utente
            ChatMessage chatMessage = ChatMessage.builder()
                    .type(MessageType.LEAVE)
                    .sender(username)
                    .build();

            messageSendingOperation.convertAndSend("/uniud/public", chatMessage);
        }else{
            System.out.println("Username incognito");
        }
    }

}
