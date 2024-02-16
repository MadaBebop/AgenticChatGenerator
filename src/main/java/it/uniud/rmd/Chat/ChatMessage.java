package it.uniud.rmd.Chat;

import it.uniud.rmd.Chat.MessageType;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ChatMessage {
    private String content;
    private String sender;
    private MessageType type;
}
