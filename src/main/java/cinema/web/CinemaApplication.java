package cinema.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/** Scans adapter configuration without direct infrastructure imports. */
@SpringBootApplication(scanBasePackages = "cinema")
public class CinemaApplication {
    public static void main(String[] args) { SpringApplication.run(CinemaApplication.class, args); }
}
