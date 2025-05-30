package dev.project.scholar_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Main class for the application.
 */
@SpringBootApplication
@CrossOrigin
public class ScholarAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScholarAiApplication.class, args);
    }
}
