package com.Web.Pharmagest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PharmagestApplication {

	public static void main(String[] args) {
		SpringApplication.run(PharmagestApplication.class, args);
	}

}
