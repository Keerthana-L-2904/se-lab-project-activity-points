package com.example.student_activity_points;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class StudentActivityPointsApplication {

    public static void main(String[] args) {
        // Load .env file
        Dotenv dotenv = Dotenv.load();

        // Example: print or use variables
        System.setProperty("MYAPP_SECRET_KEY", dotenv.get("MYAPP_SECRET_KEY"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

        SpringApplication.run(StudentActivityPointsApplication.class, args);
    }

}