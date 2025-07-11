package com.signup;

import com.signup.database.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SignupProjectApplication implements CommandLineRunner {

    @Autowired
    private DatabaseService databaseService;

    public static void main(String[] args) {
        SpringApplication.run(SignupProjectApplication.class, args);
    }



    @Override
    public void run(String... args) throws Exception {
        // Initialize database table on startup
        databaseService.initializeDatabase();
        System.out.println("Application started successfully!");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("H2 Console: http://localhost:8080/h2-console");
    }
} 