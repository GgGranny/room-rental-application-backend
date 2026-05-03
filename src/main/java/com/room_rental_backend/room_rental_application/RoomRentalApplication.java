package com.room_rental_backend.room_rental_application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableJpaAuditing
public class RoomRentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomRentalApplication.class, args);
		System.out.println("===========================================================");
		System.out.println("=														  =");
		System.out.println("=														  =");
		System.out.println("=				Room Rental Application					  =");
		System.out.println("=														  =");
		System.out.println("=														  =");
		System.out.println("===========================================================");
	}

}
