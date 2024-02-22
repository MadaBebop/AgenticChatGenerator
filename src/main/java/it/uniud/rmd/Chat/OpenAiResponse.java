package it.uniud.rmd.Chat;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenAiResponse {
    private List<Choice> choices;


    @Getter
    @Setter
    public static class Choice {
        private int index;
        private OpenAiMessage message;
    }
}
