package it.uniud.rmd.Service;

import it.uniud.rmd.Chat.OpenAiRequest;
import it.uniud.rmd.Chat.OpenAiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String openAiApiKey;
    @Value("${openai.model}")
    private String model;
    @Value("${openai.api.url}")
    private String apiUrl;
    @Qualifier("openaiRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    //Funzione di controllo del messaggio utente, se contiene il comando "!bot"
    public boolean openAiCheck(String message) {
        String[] parole = message.split("\\s+");

        // Controllo lunghezza mex
        if (parole.length > 0) {
            String primaParola = parole[0].toLowerCase();
            // Controllo "!bot"
            if(primaParola.equals("!bot")) {
                //Mi metto in moto per usare chat gpt
                System.out.println("OpenAiService.openAiCheck c'è!");
                return true;
            }
        }
        return false;
    }

    public String sendAiMessage(String messaggio) {
        OpenAiRequest request = new OpenAiRequest(model, messaggio);

        OpenAiResponse response = restTemplate.postForObject(
                apiUrl,
                request,
                OpenAiResponse.class);

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "Nessuna risposta";
        }

        return response.getChoices().get(0).getMessage().getContent();
    }
}


