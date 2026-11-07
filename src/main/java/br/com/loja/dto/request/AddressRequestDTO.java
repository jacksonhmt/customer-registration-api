package br.com.loja.dto.request;

import br.com.loja.message.AddressMessages;
import br.com.loja.validation.ValidZipCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequestDTO(
        @NotBlank(message = AddressMessages.ZIP_CODE_REQUIRED)
        @ValidZipCode
        String zipCode,

        @NotBlank(message = AddressMessages.STREET_REQUIRED)
        @Size(max = 150, message = AddressMessages.STREET_SIZE)
        String street,

        @NotBlank(message = AddressMessages.NUMBER_REQUIRED)
        @Size(max = 20, message = AddressMessages.NUMBER_SIZE)
        String number,

        @Size(max = 100, message = AddressMessages.COMPLEMENT_SIZE)
        String complement,

        @NotBlank(message = AddressMessages.NEIGHBORHOOD_REQUIRED)
        @Size(max = 100, message = AddressMessages.NEIGHBORHOOD_SIZE)
        String neighborhood,

        @NotBlank(message = AddressMessages.CITY_REQUIRED)
        @Size(max = 100, message = AddressMessages.CITY_SIZE)
        String city,

        @NotBlank(message = AddressMessages.STATE_REQUIRED)
        @Pattern(regexp = "^(?i)(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$",
                message = AddressMessages.STATE_INVALID)
        String state
) {
}
