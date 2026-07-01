package com.room_rental_backend.room_rental_application.specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.room_rental_backend.room_rental_application.models.Room;
import com.room_rental_backend.room_rental_application.models.filters.RoomSearchFilter;

import jakarta.persistence.criteria.Predicate;

public class RoomSpecifications {
    public static Specification<Room> filterRooms(
            RoomSearchFilter filter) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getLocation() != null) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("location")),
                                "%" + filter.getLocation().toLowerCase() + "%"));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("price"),
                                filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("price"),
                                filter.getMaxPrice()));
            }

            if (filter.getRoomType() != null) {
                predicates.add(
                        cb.equal(
                                root.get("roomType"),
                                filter.getRoomType()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
