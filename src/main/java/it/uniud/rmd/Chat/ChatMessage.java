package it.uniud.rmd.Chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatMessage {
    private String message;
    private String sender;
    private MessageType type;
}
