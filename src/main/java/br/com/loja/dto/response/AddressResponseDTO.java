package br.com.loja.dto.response;

public record AddressResponseDTO(
        String zipCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state
) {
}
