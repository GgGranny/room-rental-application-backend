package com.room_rental_backend.room_rental_application.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.room_rental_backend.room_rental_application.models.Property;

@Repository
public interface PropertyRepository extends JpaRepository<Property, String> {

    List<Property> findByLandlordId(String landlordId);

}
