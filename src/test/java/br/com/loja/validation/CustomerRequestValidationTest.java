package br.com.loja.validation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.loja.dto.request.AddressRequest;
import br.com.loja.dto.request.CustomerRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CustomerRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static AddressRequest address() {
        return new AddressRequest("29101000", "Avenida Principal", "100", null, "Centro", "Vila Velha", "ES");
    }

    private static CustomerRequest valid() {
        return new CustomerRequest("Maria da Silva", "52998224725", "maria@example.com", "27999999999", null,
                LocalDate.of(1990, 5, 15), null, address());
    }

    private Set<String> invalidFields(CustomerRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet());
    }

    @Test
    void validRequestHasNoViolations() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    @Test
    void optionalFieldsMayBeOmitted() {
        var request = new CustomerRequest("Maria da Silva", "52998224725", "maria@example.com", "27999999999",
                null, LocalDate.of(1990, 5, 15), null, null);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void missingMandatoryFields() {
        var request = new CustomerRequest(null, " ", "", null, null, null, null, null);
        assertThat(invalidFields(request)).containsExactlyInAnyOrder("fullName", "cpf", "email", "phoneNumber", "birthDate");
    }

    @Test
    void fullNameLength() {
        assertThat(invalidFields(new CustomerRequest("Ab", "52998224725", "a@b.com", "27999999999", null,
                LocalDate.of(1990, 1, 1), null, null))).containsExactly("fullName");
        assertThat(invalidFields(new CustomerRequest("A".repeat(151), "52998224725", "a@b.com", "27999999999", null,
                LocalDate.of(1990, 1, 1), null, null))).containsExactly("fullName");
        assertThat(invalidFields(new CustomerRequest("Abc", "52998224725", "a@b.com", "27999999999", null,
                LocalDate.of(1990, 1, 1), null, null))).isEmpty();
    }

    @Test
    void invalidCpfEmailPhoneAndFutureBirthDate() {
        var request = new CustomerRequest("Maria", "52998224724", "not-an-email", "123", "456",
                LocalDate.now().plusDays(1), null, null);
        assertThat(invalidFields(request))
                .containsExactlyInAnyOrder("cpf", "email", "phoneNumber", "additionalPhone", "birthDate");
    }

    @Test
    void addressMandatoryFieldsAndState() {
        var request = new CustomerRequest("Maria", "52998224725", "a@b.com", "27999999999", null,
                LocalDate.of(1990, 1, 1), null, new AddressRequest("123", null, " ", null, null, null, "XX"));
        assertThat(invalidFields(request)).containsExactlyInAnyOrder("address.zipCode", "address.street",
                "address.number", "address.neighborhood", "address.city", "address.state");
    }

    @Test
    void complementTooLong() {
        var request = new CustomerRequest("Maria", "52998224725", "a@b.com", "27999999999", null,
                LocalDate.of(1990, 1, 1), null,
                new AddressRequest("29101000", "Rua", "1", "x".repeat(101), "Centro", "Vila Velha", "es"));
        assertThat(invalidFields(request)).containsExactly("address.complement");
    }
}
