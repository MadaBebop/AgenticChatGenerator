package it.uniud.rmd.DTO;

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

