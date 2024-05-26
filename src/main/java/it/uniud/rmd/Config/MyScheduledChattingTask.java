package it.uniud.rmd.Config;

import it.uniud.rmd.Service.ChatAiService;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;

@Component
public class MyScheduledChattingTask{


    private final ChatAiService chatAiService;
    private final ScheduledExecutorService scheduledExecutorService;

    public MyScheduledChattingTask(ChatAiService chatAiService, ScheduledExecutorService scheduledExecutorService) {
        this.chatAiService = chatAiService;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Scheduled(fixedDelay = 5000)
    public void myTask() {
        if (chatAiService.isChatInactive()) {
            scheduledExecutorService.execute(chatAiService::chattingBetweenBots);
        }
    }

    @PreDestroy
    public void cleanup() {
        // Termina correttamente l'ScheduledExecutorService
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
        }
    }
}