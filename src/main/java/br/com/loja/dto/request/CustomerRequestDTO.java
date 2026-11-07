package br.com.loja.dto.request;

import br.com.loja.enums.Gender;
import br.com.loja.message.CustomerMessages;
import br.com.loja.validation.ValidCpf;
import br.com.loja.validation.ValidPhone;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CustomerRequestDTO(
        @NotBlank(message = CustomerMessages.FULL_NAME_REQUIRED)
        @Size(min = 3, max = 150, message = CustomerMessages.FULL_NAME_SIZE)
        String fullName,

        @NotBlank(message = CustomerMessages.CPF_REQUIRED)
        @ValidCpf
        String cpf,

        @NotBlank(message = CustomerMessages.EMAIL_REQUIRED)
        @Email(message = CustomerMessages.EMAIL_INVALID)
        @Size(max = 150, message = CustomerMessages.EMAIL_SIZE)
        String email,

        @NotBlank(message = CustomerMessages.PHONE_REQUIRED)
        @ValidPhone
        String phoneNumber,

        @ValidPhone
        String additionalPhone,

        @NotNull(message = CustomerMessages.BIRTH_DATE_REQUIRED)
        @PastOrPresent(message = CustomerMessages.BIRTH_DATE_NOT_FUTURE)
        LocalDate birthDate,

        Gender gender,

        @Valid
        AddressRequestDTO address
) {
}
