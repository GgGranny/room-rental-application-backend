package com.room_rental_backend.room_rental_application;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class RoomRentalApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Container
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.datasource.url", postgres::getJdbcUrl);
		dynamicPropertyRegistry.add("spring.datasource.username", postgres::getUsername);
		dynamicPropertyRegistry.add("spring.datasource.password", postgres::getPassword);
	}

	@Test
	void testConnecton() {
		String jdbcUrl = postgres.getJdbcUrl();
		assertTrue(postgres.isRunning());
	}

}
