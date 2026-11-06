package br.com.loja.dto.request;

import br.com.loja.validation.ValidZipCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "ZIP code (CEP) is required.")
        @ValidZipCode
        String zipCode,

        @NotBlank(message = "Street is required.")
        @Size(max = 150, message = "Street must have at most 150 characters.")
        String street,

        @NotBlank(message = "Number is required.")
        @Size(max = 20, message = "Number must have at most 20 characters.")
        String number,

        @Size(max = 100, message = "Complement must have at most 100 characters.")
        String complement,

        @NotBlank(message = "Neighborhood is required.")
        @Size(max = 100, message = "Neighborhood must have at most 100 characters.")
        String neighborhood,

        @NotBlank(message = "City is required.")
        @Size(max = 100, message = "City must have at most 100 characters.")
        String city,

        @NotBlank(message = "State is required.")
        @Pattern(regexp = "^(?i)(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$",
                message = "State must be a valid Brazilian UF.")
        String state
) {
}
