package it.uniud.rmd.Config;

import it.uniud.rmd.Service.OpenAiService;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;

@Component
public class MyScheduledChattingTask{

    private final OpenAiService service;
    private final ScheduledExecutorService scheduledExecutorService;

    public MyScheduledChattingTask(OpenAiService service, ScheduledExecutorService scheduledExecutorService) {
        this.service = service;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Scheduled(fixedDelay = 5000)
    public void myTask() {
        if (service.isChatInactive()) {
            scheduledExecutorService.execute(service::chattingBetweenBots);
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