package concert.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "concert.infrastructure", "concert.domain", "concert.commons", "concert.application"})
public class ConcertApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConcertApplication.class, args);
	}
}
