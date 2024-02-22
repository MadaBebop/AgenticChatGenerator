package it.uniud.rmd.DTO;

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

    //Costruttore con storico messaggi
    public OpenAiRequest(String model, List<OpenAiMessage> messages, double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }
}
