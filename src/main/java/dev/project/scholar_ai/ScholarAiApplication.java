package dev.project.scholar_ai;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class for the application.
 */
@SpringBootApplication
public class ScholarAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScholarAiApplication.class, args);
    }

}
