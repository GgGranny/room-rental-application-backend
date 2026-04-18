package com.room_rental_backend.room_rental_application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RoomRentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomRentalApplication.class, args);
	}

}
