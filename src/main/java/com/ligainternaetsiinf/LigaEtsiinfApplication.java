package com.ligainternaetsiinf;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LigaEtsiinfApplication {

	public static void main(String[] args) {
		// Zona horaria única para toda la aplicación: todas las fechas (LocalDateTime.now(),
		// tareas programadas, crons...) se interpretan en hora peninsular española,
		// independientemente de la zona horaria del servidor donde se despliegue.
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Madrid"));
		SpringApplication.run(LigaEtsiinfApplication.class, args);
	}

}
