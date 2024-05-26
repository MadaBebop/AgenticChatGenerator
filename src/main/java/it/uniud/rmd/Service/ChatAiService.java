package it.uniud.rmd.Service;

import it.uniud.rmd.DTO.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class ChatAiService {

    @Value("${openai.model}")
    private String model;
    @Value("${openai.api.url}")
    private String apiUrl;
    @Value("${openai.temperature}")
    private Double temperature;

    @Qualifier("openairesttemplate")
    private RestTemplate restTemplate;

    private final SimpMessagingTemplate messagingTemplate;

    //AtomicLong tiene traccia delle modifiche apportate alla variabile anche in contesto di multi threading
    private static final long TIMEOUT_SECONDS = 10;
    private volatile long lastChatActivityTimestamp;


    //Costruttore
    public ChatAiService(SimpMessagingTemplate messagingTemplate, RestTemplate restTemplate){
        //Templates V.
        this.messagingTemplate = messagingTemplate;
        this.restTemplate = restTemplate;

        //Timestamp chat initialization
        lastChatActivityTimestamp = System.currentTimeMillis();//First Timestamp
    }


    public void chattingBetweenBots (){
        //botText contiene alla partenza il primo messaggio della conversazione (di Vortex)
        String botText = "Parlami di un argomento qualsiasi";
        String systemIndications = "intrattieni una conversazione";

        //Creo il thread di Vortex
        List<OpenAiMessage> conversationVortex = new ArrayList<>();
        conversationVortex.add(new OpenAiMessage("system", systemIndications));
        conversationVortex.add(new OpenAiMessage("user", botText));


        //Creo il thread di Pulsar
        List<OpenAiMessage> conversationPulsar = new ArrayList<>();
        conversationPulsar.add(new OpenAiMessage("system", systemIndications));
        conversationPulsar.add(new OpenAiMessage("assistant", botText));

        //Check della conversazione
        boolean conversationAlternation = true;

        //Avvio il ciclo di conversazione
        while(isChatInactive()){
            String conversationTurn = conversationAlternation ? "vortex" : "pulsar";

            if(conversationTurn.equals("vortex")) {
                String response = sendApiRequest(conversationVortex);
                //Aggiorno la chat
                newChatMessage(new ChatMessage(
                        response,
                        "Vortex",
                        MessageType.CHAT
                ));

                conversationVortex.add(new OpenAiMessage("assistant", response));
                conversationPulsar.add(new OpenAiMessage("user", response));
            }else if(conversationTurn.equals("pulsar")){
                String response = sendApiRequest(conversationPulsar);
                //Aggiorno la chat
                newChatMessage(new ChatMessage(
                        response,
                        "Pulsar",
                        MessageType.CHAT
                ));

                conversationPulsar.add(new OpenAiMessage("assistant", response));
                conversationVortex.add(new OpenAiMessage("user", response));
            }

            conversationAlternation = !conversationAlternation;

            //Awaits few secs
            try {
                Thread.sleep(4000);
            }catch (InterruptedException ignored){
                System.out.println("chattingBetweenBots IE error");
            }
        }
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


    public String sendApiRequest(List<OpenAiMessage> conversation) {

        double temperatura = temperature;

        //Si ha un solo messaggio quando usiamo l'assistente AI
        if(conversation.size() == 1){
            //Vogliamo che la risposta sia il più deterministica possibile
            temperatura = 0;
        }

        OpenAiRequest request = new OpenAiRequest(
                model,
                conversation,
                temperatura
        );

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

            //Prendo una risposta a caso dalla List<Choice> choices
            Random random = new Random();
            int randomValue = random.nextInt(response.getChoices().size());
            //Ritorno il messaggio generato dall'API
            return response.getChoices().get(randomValue).getMessage().getContent();

        }catch(Exception e) {
            System.out.println("é stato impossibile accedere all'api");
        }

        return "failed request";
    }

    //Inserisco un nuovo messaggio nella chat
    public void newChatMessage(ChatMessage messageToElaborate) {

        messagingTemplate.convertAndSend("/uniud/public", new ChatMessage(
                messageToElaborate.getContent(),
                messageToElaborate.getSender(),
                MessageType.CHAT)
        );
    }
}


