package it.uniud.rmd.Chat;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OpenAiRequest {
    private String model;
    private List<OpenAiMessage> messages;
    private double temperature;

    public OpenAiRequest(String model, String prompt, double temperature) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new OpenAiMessage("user", prompt));
        this.temperature = temperature;
    }

    //Costruttore con specifica di contesto
    public OpenAiRequest(String model, String prompt, String specifications, double temperature) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new OpenAiMessage("system",specifications));
        this.messages.add(new OpenAiMessage("user", prompt));
        this.temperature = temperature;
    }
}
