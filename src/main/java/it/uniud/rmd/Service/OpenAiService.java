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
public class OpenAiService {

    @Value("${openai.model}")
    private String model;
    @Value("${openai.api.url}")
    private String apiUrl;

    @Qualifier("openairesttemplate")
    private RestTemplate restTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    //AtomicLong tiene traccia delle modifiche apportate alla variabile anche in contesto di multi threading
    private static final long TIMEOUT_SECONDS = 30;
    private volatile long lastChatActivityTimestamp;
    private volatile boolean isChatActive;

    //Thread per la chat dei bot
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    //Costruttore
    public OpenAiService(SimpMessagingTemplate messagingTemplate, RestTemplate restTemplate){
        this.messagingTemplate = messagingTemplate;
        this.restTemplate = restTemplate;

        //Timestamp chat initialization
        lastChatActivityTimestamp = System.currentTimeMillis();//First Timestamp
        isChatActive= true;
    }


    @Scheduled(fixedRate = 2000)
    public void checkChatStatus() {
        long currentTime = System.currentTimeMillis();
        long idleTime = currentTime - lastChatActivityTimestamp;

        if (idleTime >= TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)) {
            // La chat è inattiva da più di tot secondi, avvia la chat tra bot
            isChatActive(false);
            //TODO: da migliorare la creazione del thread. Problema: si entra in un loop nel quale l'if ripete la creazione del thread.
            executorService.execute(this::chattingBetweenBots);
        }
    }

    public void updateLastChatActivity() {
        lastChatActivityTimestamp = System.currentTimeMillis();
    }


    public void chattingBetweenBots (){
        String botText = "Inizia una conversazione su un tuo argomento a piacere.";
        boolean alternateBotId = true;//Booleano che identifica il bot 1 (true) e il bot 2 (false)

        while(!isChatActive){
            String botId = alternateBotId ? "Vortex" : "Pulsar";
            elaborateAnswareBot(new ChatMessage(botText,botId , MessageType.CHAT));
            botText = sendAiMessage(botText);
            alternateBotId=(!alternateBotId);
            try {
                Thread.sleep(2500);
            }catch (InterruptedException ignored){
                System.out.println("chattingBetweenBots IE error");
            }
        }
    }

    public void isChatActive(boolean status){
        isChatActive = status;
    }

    //Funzione di controllo del messaggio utente, se contiene il comando "!bot"
    public boolean openAiCheck(String message) {
        // Controllo se viene usato il comando "!bot"
        return message.toLowerCase().startsWith("!bot");
    }

    //Invio una richiesta api al bot di chat gpt, mi aspetto una sua risposta
    public String sendAiMessage(String messaggio) {
        OpenAiRequest request = new OpenAiRequest(model, messaggio);
        System.out.println("Generato il messaggio");
        try {
            OpenAiResponse response = restTemplate.postForObject(
                    apiUrl,
                    request,
                    OpenAiResponse.class);

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

    public void notifyBotsLeavingChatRoom(){
        //Notifichiamo l'uscita dei due bot dalla chat.
        messagingTemplate.convertAndSend("/uniud/public", new ChatMessage(
                "Bye!",
                "Vortex",
                MessageType.LEAVE
        ));
        messagingTemplate.convertAndSend("/uniud/public", new ChatMessage(
                "Bye!",
                "Pulsar",
                MessageType.LEAVE
        ));
    }
}


