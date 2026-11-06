package br.com.loja.dto.response;

public record AddressResponse(
        String zipCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state
) {
}
