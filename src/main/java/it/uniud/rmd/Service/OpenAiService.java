package it.uniud.rmd.Service;

import org.springframework.stereotype.Service;

@Service
public class OpenAiService {

    //Funzione di controllo del messaggio utente, se contiene il comando "!bot"
    public void openAiCheck(String message) {
        String[] parole = message.split("\\s+");

        // Controllo lunghezza mex
        if (parole.length > 0) {
            String primaParola = parole[0].toLowerCase();
            // Controllo "!bot"
            if(primaParola.equals("!bot")) {
                //Mi metto in moto per usare chat gpt
                System.out.println("OpenAiService.openAiCheck c'è!");
            }
        }
    }

}
