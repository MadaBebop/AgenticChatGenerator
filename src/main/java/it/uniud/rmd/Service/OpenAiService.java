package it.uniud.rmd.Service;

import it.uniud.rmd.Chat.ChatMessage;
import it.uniud.rmd.Chat.MessageType;
import it.uniud.rmd.Chat.OpenAiRequest;
import it.uniud.rmd.Chat.OpenAiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String openAiApiKey;
    @Value("${openai.model}")
    private String model;
    @Value("${openai.api.url}")
    private String apiUrl;

    @Qualifier("openairesttemplate")
    private RestTemplate restTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    //lastchatactivitytimestamp tiene traccia dell'inutilizzo della chat
    private volatile long lastChatActivityTimestamp;
    private final ScheduledExecutorService scheduler;

    public OpenAiService(SimpMessagingTemplate messagingTemplate, RestTemplate restTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.restTemplate = restTemplate;

        //Timestamp chat initialization
        this.lastChatActivityTimestamp = System.currentTimeMillis(); //First Timestamp
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        startInactivityCheck();
    }

    public void startInactivityCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            long inactiveDuration = currentTime - lastChatActivityTimestamp;

            if (inactiveDuration > 20000) { // 20 secondi in millisecondi
                // Invia il messaggio di sistema
                System.out.println("OpenAiService.startInactivityCheck");
            }
        }, 1, 2, TimeUnit.SECONDS);
    }

    public void updateLastChatActivity() {
        lastChatActivityTimestamp = System.currentTimeMillis();
    }

    //Funzione di controllo del messaggio utente, se contiene il comando "!bot"
    public boolean openAiCheck(String message) {
            // Controllo "!bot"
            if(message.toLowerCase().startsWith("!bot")) {
                //Mi metto in moto per usare chat gpt
                System.out.println("OpenAiService.openAiCheck c'è!");
                return true;
            }
        return false;
    }

    public String sendAiMessage(String messaggio) {
        OpenAiRequest request = new OpenAiRequest(model, messaggio);
        System.out.println("Generato il messaggio");
        OpenAiResponse response = restTemplate.postForObject(
                apiUrl,
                request,
                OpenAiResponse.class);
        System.out.println("inviato la richiesta");
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "Nessuna risposta";
        }
        System.out.println("ritorno");
        return response.getChoices().get(0).getMessage().getContent();
    }

    public void elaborateBotCommand(ChatMessage messageToElaborate) {
        String rispostaBot = sendAiMessage(messageToElaborate.getContent());
        messagingTemplate.convertAndSend("/uniud/public", new ChatMessage(
                rispostaBot,
                "OpenAiBot",
                MessageType.CHAT)
        );
    }

    //Controllo se la chat non viene utilizzata da tempo
    public boolean checkChatStatus (){
        return true;
    }
}


