package com.room_rental_backend.room_rental_application.mappers;

import org.springframework.stereotype.Component;

import com.room_rental_backend.room_rental_application.dtos.requestDtos.AddressDataRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.ContactDataRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.DocumentDataReqeust;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.KycRequest;
import com.room_rental_backend.room_rental_application.dtos.requestDtos.PersonalDataRequest;
import com.room_rental_backend.room_rental_application.dtos.responseDtos.KycResponse;
import com.room_rental_backend.room_rental_application.enums.DocumentTypes;
import com.room_rental_backend.room_rental_application.mappers.generice_mapper.GenericMapper;
import com.room_rental_backend.room_rental_application.models.Kyc;

@Component
public class KycMapper implements GenericMapper<Kyc, KycRequest> {
        @Override
        public KycRequest toDto(Kyc entity) {
                AddressDataRequest address = AddressDataRequest.builder()
                                .addressLine1(entity.getAddressLine1())
                                .addressLine2(entity.getAddressLine2() != null ? entity.getAddressLine2() : "")
                                .city(entity.getCity())
                                .postalCode(entity.getPostalCode())
                                .state(entity.getState())
                                .build();

                DocumentDataReqeust document = DocumentDataReqeust.builder()
                                .build();

                PersonalDataRequest personalDataRequest = PersonalDataRequest.builder()
                                .build();

                ContactDataRequest cotactData = ContactDataRequest.builder()
                                .build();

                return KycRequest.builder()
                                .address(address)
                                .contact(cotactData)
                                .document(document)
                                .personalInfo(personalDataRequest)
                                .build();
        }

        public KycResponse toResponse(Kyc entity) {
                return KycResponse.builder()
                                .kycId(entity.getId())
                                .customerId(entity.getUser() != null ? entity.getUser().getId() : null)
                                .kycStatus(entity.getStatus())
                                .submittedAt(entity.getSubmittedAt())
                                .firstName(entity.getFirstName())
                                .lastName(entity.getLastName())
                                .middleName(entity.getMiddleName())
                                .dateOfBirth(entity.getDateOfBirth())
                                .gender(entity.getGender())
                                .documentType(entity.getDocumentType())
                                .frontImageUrl(entity.getFrontImageUrl())
                                .backImageUrl(entity.getBackImageUrl())
                                .selfieUrl(entity.getSelfieUrl())
                                .addressLine1(entity.getAddressLine1())
                                .addressLine2(entity.getAddressLine2())
                                .city(entity.getCity())
                                .state(entity.getState())
                                .postalCode(entity.getPostalCode())
                                .country(entity.getCountry())
                                .phoneNumber(entity.getPhoneNumber())
                                .alternatePhone(entity.getAltPhoneNumber())
                                .build();
        }

        @Override
        public Kyc toEntity(KycRequest dto) {
                String address2 = trimToNull(dto.getAddress().getAddressLine2());
                String middleName = dto.getPersonalInfo().getMiddleName();
                String altPhoneNumber = trimToNull(dto.getContact().getAlternatePhone());

                DocumentTypes documentType = parseDocumentType(dto.getDocument().getDocumentType());
                return Kyc.builder()
                                .addressLine1(dto.getAddress().getAddressLine1())
                                .addressLine2(address2)
                                .dateOfBirth(dto.getPersonalInfo().getDateOfBirth())
                                .country(dto.getAddress().getCountry())
                                .city(dto.getAddress().getCity())
                                .state(dto.getAddress().getState())
                                .postalCode(dto.getAddress().getPostalCode())
                                .dateOfBirth(dto.getPersonalInfo().getDateOfBirth())
                                .firstName(dto.getPersonalInfo().getFirstName())
                                .lastName(dto.getPersonalInfo().getLastName())
                                .middleName(trimToNull(middleName))
                                .gender(dto.getPersonalInfo().getGender())
                                .frontImageUrl(dto.getDocument().getFrontImageUrl())
                                .backImageUrl(dto.getDocument().getBackImageUrl())
                                .selfieUrl(dto.getDocument().getSelfieUrl())
                                .documentType(documentType)
                                .phoneNumber(dto.getContact().getPhoneNumber())
                                .altPhoneNumber(altPhoneNumber)
                                .build();
        }

        public Kyc updateEntity(Kyc entity, KycRequest dto) {
                entity.setFirstName(dto.getPersonalInfo().getFirstName());
                entity.setLastName(dto.getPersonalInfo().getLastName());
                entity.setMiddleName(trimToNull(dto.getPersonalInfo().getMiddleName()));
                entity.setDateOfBirth(dto.getPersonalInfo().getDateOfBirth());
                entity.setGender(dto.getPersonalInfo().getGender());
                entity.setDocumentType(parseDocumentType(dto.getDocument().getDocumentType()));
                entity.setAddressLine1(dto.getAddress().getAddressLine1());
                entity.setAddressLine2(trimToNull(dto.getAddress().getAddressLine2()));
                entity.setCity(dto.getAddress().getCity());
                entity.setState(dto.getAddress().getState());
                entity.setPostalCode(dto.getAddress().getPostalCode());
                entity.setCountry(dto.getAddress().getCountry());
                entity.setPhoneNumber(dto.getContact().getPhoneNumber());
                entity.setAltPhoneNumber(trimToNull(dto.getContact().getAlternatePhone()));
                return entity;
        }

        private DocumentTypes parseDocumentType(String value) {
                String normalizedValue = value.trim().toUpperCase();
                if (normalizedValue.equals("LICENSE")) {
                        normalizedValue = "DRIVERS_LICENSE";
                }
                return DocumentTypes.valueOf(normalizedValue);
        }

        private String trimToNull(String value) {
                if (value == null || value.trim().isEmpty()) {
                        return null;
                }
                return value.trim();
        }

}
