package it.uniud.rmd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;


@SpringBootApplication
public class UniUdRmdApplication {
    public static void main(String[] args) {
        SpringApplication.run(UniUdRmdApplication.class, args);
    }
}
