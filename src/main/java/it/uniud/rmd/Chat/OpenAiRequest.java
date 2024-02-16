package it.uniud.rmd.Chat;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OpenAiRequest {
    private final String model;
    private List<OpenAiMessage> messages;

    public OpenAiRequest(String model, String prompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new OpenAiMessage("user", prompt));
    }
}
