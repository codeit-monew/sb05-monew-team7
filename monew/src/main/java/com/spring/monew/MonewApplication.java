package com.spring.monew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonewApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonewApplication.class, args);
		System.out.println("http://localhost:8080");
	}

}
