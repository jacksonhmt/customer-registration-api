package br.com.loja.dto.request;

import br.com.loja.entity.Gender;
import br.com.loja.validation.ValidCpf;
import br.com.loja.validation.ValidPhone;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CustomerRequest(
        @NotBlank(message = "Full name is required.")
        @Size(min = 3, max = 150, message = "Full name must have between 3 and 150 characters.")
        String fullName,

        @NotBlank(message = "CPF is required.")
        @ValidCpf
        String cpf,

        @NotBlank(message = "Email is required.")
        @Email(message = "The provided email is invalid.")
        @Size(max = 150, message = "Email must have at most 150 characters.")
        String email,

        @NotBlank(message = "Phone number is required.")
        @ValidPhone
        String phoneNumber,

        @ValidPhone
        String additionalPhone,

        @NotNull(message = "Birth date is required.")
        @PastOrPresent(message = "Birth date cannot be in the future.")
        LocalDate birthDate,

        Gender gender,

        @Valid
        AddressRequest address
) {
}
