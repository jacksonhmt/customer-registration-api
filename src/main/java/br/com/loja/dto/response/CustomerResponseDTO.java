package br.com.loja.dto.response;

import br.com.loja.enums.Gender;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** CPF is intentionally not exposed in responses (LGPD data minimization). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomerResponseDTO(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        String additionalPhone,
        LocalDate birthDate,
        Gender gender,
        AddressResponseDTO address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
