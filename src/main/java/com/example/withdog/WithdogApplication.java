package com.example.withdog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WithdogApplication {

	public static void main(String[] args) {
		SpringApplication.run(WithdogApplication.class, args);
	}

}
