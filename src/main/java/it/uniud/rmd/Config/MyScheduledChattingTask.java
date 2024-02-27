package it.uniud.rmd.Config;

import it.uniud.rmd.Service.OpenAiService;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;

@Component
public class MyScheduledChattingTask{


    private final OpenAiService openAiService;
    private final ScheduledExecutorService scheduledExecutorService;

    public MyScheduledChattingTask(OpenAiService openAiService, ScheduledExecutorService scheduledExecutorService) {
        this.openAiService = openAiService;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Scheduled(fixedDelay = 5000)
    public void myTask() {
        if (openAiService.isChatInactive()) {
            scheduledExecutorService.execute(openAiService::chattingBetweenBots);
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