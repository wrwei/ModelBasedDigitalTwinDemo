package com.anadeem.dt_process_model;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class DtProcessModelApplication {

	public static void main(String[] args) {
		SpringApplication.run(DtProcessModelApplication.class, args);
	}
}
