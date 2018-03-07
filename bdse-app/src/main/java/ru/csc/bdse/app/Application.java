package ru.csc.bdse.app;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static final String V1_PROFILE = "v1";
    public static final String V2_PROFILE = "v2";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
