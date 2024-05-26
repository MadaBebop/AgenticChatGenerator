package it.uniud.rmd.Controller;

import it.uniud.rmd.DTO.*;
import it.uniud.rmd.Service.ChatAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

//Mappato di default "/"
@Controller
public class ChatAppController {

    private final ChatAiService chatAiService;

    @Autowired
    public ChatAppController(ChatAiService chatAiService) {
        this.chatAiService = chatAiService;
    }

    //Ritorna la pagina della chat: "home.html"
    @GetMapping("/")
    public String getHome() {
        return "home";
    }

    /*
     "@MessageMapping": si assicura che, se un messaggio viene inviato alla destinazione "/sendmessage",
     venga chiamato il metodo sendMessage().
     "SendTo": definisce la queue dove il messaggio verrà inoltrato.
     "/uniud": viene definito nelle config dal "enableSimpleBroker".
     */
    @MessageMapping("/sendmessage")
    @SendTo("/uniud/public")
    public ChatMessage sendMessage (@Payload ChatMessage message){
        //Aggiornamento status attività chat
        chatAiService.updateLastChatActivity();
        System.out.println("Aggiornamento del timer inattività");

        //Controllo esistenza comando "!bot"
        if(chatAiService.openAiCheck(message.getContent())) {

            //Aggiungo il messaggio a una lista conversazione che richiede l'API
            List<OpenAiMessage> messaggio = new ArrayList<>(){};
            messaggio.add(
                    new OpenAiMessage(
                            "user",
                            message.getContent()
                    )
            );

            //avvio un thread secondario asincrono
            Executors.newSingleThreadExecutor().execute(() ->
                    chatAiService.newChatMessage(
                            new ChatMessage(
                                    chatAiService.sendApiRequest(messaggio),
                                    "OpenAiBot",
                                    MessageType.CHAT
                            )
                    )
            );
        }

        return message;
    }

    @MessageMapping("/adduser")
    @SendTo("/uniud/public")
    public ChatMessage addUser(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor){
        //Aggiungiamo lo username del ChatMessage nel canale websocket: mappato con "username"
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", message.getSender());
        return message;
    }
}
