package it.uniud.rmd.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;

@Configuration
@EnableScheduling
public class ScheduledTaskConfig {

    @Bean
    public ScheduledExecutorFactoryBean scheduledExecutorService() {
        ScheduledExecutorFactoryBean executorFactoryBean = new ScheduledExecutorFactoryBean();
        executorFactoryBean.setPoolSize(1); // Imposta il numero di thread su 1 solo
        return executorFactoryBean;
    }
}
