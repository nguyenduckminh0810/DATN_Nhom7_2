package com.auro.auro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class AuroApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuroApplication.class, args);
	}

}
