package com.example.baohg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.baohg.repository")
public class BaohgApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaohgApplication.class, args);
	}

}
