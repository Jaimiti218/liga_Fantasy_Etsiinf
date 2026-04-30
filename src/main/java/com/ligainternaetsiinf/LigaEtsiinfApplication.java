package com.ligainternaetsiinf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LigaEtsiinfApplication {

	public static void main(String[] args) {
		SpringApplication.run(LigaEtsiinfApplication.class, args);
	}

}
