package br.com.loja.service;

import br.com.loja.dto.request.AddressRequest;
import br.com.loja.dto.request.CustomerRequest;
import br.com.loja.dto.response.AddressResponse;
import br.com.loja.dto.response.CustomerResponse;
import br.com.loja.entity.Address;
import br.com.loja.entity.Customer;
import br.com.loja.validation.BrazilianData;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public static String normalizeCpf(String cpf) {
        return BrazilianData.digitsOnly(cpf);
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public void apply(CustomerRequest request, Customer customer) {
        customer.setFullName(request.fullName().trim());
        customer.setCpf(normalizeCpf(request.cpf()));
        customer.setEmail(normalizeEmail(request.email()));
        customer.setPhoneNumber(BrazilianData.normalizePhone(request.phoneNumber()));
        customer.setAdditionalPhone(request.additionalPhone() == null || request.additionalPhone().isBlank()
                ? null : BrazilianData.normalizePhone(request.additionalPhone()));
        customer.setBirthDate(request.birthDate());
        customer.setGender(request.gender());

        if (request.address() == null) {
            customer.setAddress(null);
        } else {
            Address address = customer.getAddress() != null ? customer.getAddress() : new Address();
            apply(request.address(), address);
            customer.setAddress(address);
        }
    }

    private void apply(AddressRequest request, Address address) {
        address.setZipCode(BrazilianData.digitsOnly(request.zipCode()));
        address.setStreet(request.street().trim());
        address.setNumber(request.number().trim());
        address.setComplement(request.complement() == null || request.complement().isBlank()
                ? null : request.complement().trim());
        address.setNeighborhood(request.neighborhood().trim());
        address.setCity(request.city().trim());
        address.setState(request.state().trim().toUpperCase(Locale.ROOT));
    }

    public CustomerResponse toResponse(Customer customer) {
        Address a = customer.getAddress();
        AddressResponse address = a == null ? null : new AddressResponse(a.getZipCode(), a.getStreet(),
                a.getNumber(), a.getComplement(), a.getNeighborhood(), a.getCity(), a.getState());
        return new CustomerResponse(customer.getId(), customer.getFullName(), customer.getEmail(),
                customer.getPhoneNumber(), customer.getAdditionalPhone(), customer.getBirthDate(),
                customer.getGender(), address, customer.getCreatedAt(), customer.getUpdatedAt());
    }
}
