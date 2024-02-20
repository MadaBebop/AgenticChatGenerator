package it.uniud.rmd.Controller;

import it.uniud.rmd.Chat.*;
import it.uniud.rmd.Service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Objects;
import java.util.concurrent.Executors;

//Mappato di default "/"
@Controller
public class ChatAppController {

    private final OpenAiService openAiService;

    @Autowired
    public ChatAppController(OpenAiService openAiService) {
        this.openAiService = openAiService;
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
        //Controllo esistenza comando "!bot"
        if(openAiService.openAiCheck(message.getContent())) {
            //avvio un thread secondario asincrono
            Executors.newSingleThreadExecutor().execute(() ->
                    openAiService.elaborateAnswareBot(new ChatMessage(
                            message.getContent(),
                            "OpenAiBot",
                            MessageType.CHAT)
                    ));
        }
        //Aggiornamento status chat dall'ultimo messaggio inviato
        openAiService.updateLastChatActivity();
        System.out.println("Aggiornamento del timer inattività");
        openAiService.isChatActive(true);

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
