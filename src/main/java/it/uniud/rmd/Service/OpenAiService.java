package it.uniud.rmd.Service;

import it.uniud.rmd.Chat.ChatMessage;
import it.uniud.rmd.Chat.MessageType;
import it.uniud.rmd.Chat.OpenAiRequest;
import it.uniud.rmd.Chat.OpenAiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class OpenAiService{

    @Value("${openai.model}")
    private String model;
    @Value("${openai.api.url}")
    private String apiUrl;
    @Value("${openai.temperature}")
    private Double temperature;

    @Qualifier("openairesttemplate")
    private RestTemplate restTemplate;

    private final SimpMessagingTemplate messagingTemplate;
    private final ExecutorService singleThreadExecutor;

    //AtomicLong tiene traccia delle modifiche apportate alla variabile anche in contesto di multi threading
    private static final long TIMEOUT_SECONDS = 10;
    private volatile long lastChatActivityTimestamp;


    //Costruttore
    public OpenAiService(SimpMessagingTemplate messagingTemplate, RestTemplate restTemplate){
        //Templates V.
        this.messagingTemplate = messagingTemplate;
        this.restTemplate = restTemplate;

        singleThreadExecutor= Executors.newSingleThreadExecutor();
        //Timestamp chat initialization
        lastChatActivityTimestamp = System.currentTimeMillis();//First Timestamp
    }


    @Scheduled(fixedRate = 1000)
    public void checkChatStatus() {
        if (isChatInactive()) {
                startThread();
        }
    }

    public void chattingBetweenBots (){
        String botText = sendAiMessage("Parlami di qualcosa a piacere");
        boolean alternateBotId = true; //Booleano che identifica il bot 1 (true) e il bot 2 (false)

        notifyBotsEnteringChatRoom();
        while(isChatInactive()){
            //Awaits few secs
            try {
                Thread.sleep(500);
            }catch (InterruptedException ignored){
                System.out.println("chattingBetweenBots IE error");
            }
            String botId = alternateBotId ? "Vortex" : "Pulsar";
            elaborateAnswareBot(new ChatMessage(botText,botId , MessageType.CHAT));
            botText = sendAiMessageWithInput(botText, "Stai conversando con un altro bot");
            alternateBotId=(!alternateBotId);
        }
    }

    public void startThread() {
        singleThreadExecutor.execute(this::chattingBetweenBots);
    }

    public boolean isTerminateThread() {
        return singleThreadExecutor.isShutdown();
    }

    public boolean isChatInactive(){
        //Controllo dell'inattività
        long idleTime = System.currentTimeMillis() - lastChatActivityTimestamp;
        return (idleTime >= TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
    }

    public void updateLastChatActivity() {
        lastChatActivityTimestamp = System.currentTimeMillis();
    }

    //Funzione di controllo del messaggio utente, se contiene il comando "!bot"
    public boolean openAiCheck(String message) {
        // Controllo se viene usato il comando "!bot"
        return message.toLowerCase().startsWith("!bot");
    }

    //Invio una richiesta api al bot di chat gpt, mi aspetto una sua risposta
    public String sendAiMessage(String messaggio) {
        OpenAiRequest request = new OpenAiRequest(model, messaggio, temperature);

        try {
            OpenAiResponse response = restTemplate.postForObject(
                    apiUrl,
                    request,
                    OpenAiResponse.class
            );
            System.out.println("Generata la richiesta");

            if(response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                return "Nessuna risposta, il bot potrebbe essere occupato :(";
            }

            return response.getChoices().get(0).getMessage().getContent();

        }catch(Exception e) {
            System.out.println("é stato impossibile accedere all'api");
        }

        return "failed request";
    }

    public String sendAiMessageWithInput(String messaggio, String specifications) {
        OpenAiRequest request = new OpenAiRequest(model, messaggio,specifications, temperature);

        try {
            OpenAiResponse response = restTemplate.postForObject(
                    apiUrl,
                    request,
                    OpenAiResponse.class
            );
            System.out.println("Generata la richiesta");

            if(response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                return "Nessuna risposta, il bot potrebbe essere occupato :(";
            }

            return response.getChoices().get(0).getMessage().getContent();

        }catch(Exception e) {
            System.out.println("é stato impossibile accedere all'api");
        }

        return "failed request";
    }

    //Invia il messaggio di risposta del bot nella web chat
    public void elaborateAnswareBot(ChatMessage messageToElaborate) {
        String rispostaBot = sendAiMessage(messageToElaborate.getContent());
        messagingTemplate.convertAndSend("/uniud/public", new ChatMessage(
                rispostaBot,
                messageToElaborate.getSender(),
                MessageType.CHAT)
        );
    }

    public void notifyBotsEnteringChatRoom(){
        //Notifichiamo la presenza dei due bot in chat.
        messagingTemplate.convertAndSend("/uniud/public", new ChatMessage(
                "",
                "Vortex",
                MessageType.JOIN
        ));
        messagingTemplate.convertAndSend("/uniud/public", new ChatMessage(
                "",
                "Pulsar",
                MessageType.JOIN
        ));
    }

}


