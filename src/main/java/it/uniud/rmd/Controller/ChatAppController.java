package it.uniud.rmd.Controller;

import it.uniud.rmd.Chat.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
public class ChatAppController {

    /**
     "@MessageMapping": si assicura che, se un messaggio viene inviato alla destinazione "/sendmessage",
     venga chiamato il metodo sendMessage().
     "SendTo": definisce la queue dove il messaggio verrà inoltrato.
     "/uniud": viene definito nelle config dal "enableSimpleBroker".
     */
    @MessageMapping("/sendMessage")
    @SendTo("/uniud/public")
    public ChatMessage sendMessage(@Payload ChatMessage message){
        return message;
    }


    @MessageMapping("/addUser")
    @SendTo("/uniud/public")
    public ChatMessage addUser(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor){

        //Aggiungiamo lo username del ChatMessage nel canale WS : mappato con "username"
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", message.getSender());
        return message;
    }
}
