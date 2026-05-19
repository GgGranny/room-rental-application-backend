package com.room_rental_backend.room_rental_application.mappers.generice_mapper;

public interface GenericMapper<E, D> {
    D toDto(E entity);

    E toEntity(D dto);
}
