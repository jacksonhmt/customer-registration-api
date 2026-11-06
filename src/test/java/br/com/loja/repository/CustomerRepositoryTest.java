package br.com.loja.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.loja.entity.Address;
import br.com.loja.entity.Customer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    CustomerRepository repository;

    private Customer customer(String cpf, String email) {
        Customer c = new Customer();
        c.setFullName("Maria da Silva");
        c.setCpf(cpf);
        c.setEmail(email);
        c.setPhoneNumber("27999999999");
        c.setBirthDate(LocalDate.of(1990, 5, 15));
        return c;
    }

    @Test
    void persistsCustomerWithAddress() {
        Customer c = customer("52998224725", "a@example.com");
        Address a = new Address();
        a.setZipCode("29101000");
        a.setStreet("Rua");
        a.setNumber("1");
        a.setNeighborhood("Centro");
        a.setCity("Vila Velha");
        a.setState("ES");
        c.setAddress(a);

        repository.saveAndFlush(c);

        Customer found = repository.findByIdAndDeletedAtIsNull(c.getId()).orElseThrow();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getAddress().getState()).isEqualTo("ES");
        assertThat(repository.findByCpf("52998224725")).isPresent();
        assertThat(repository.existsByEmail("a@example.com")).isTrue();
    }

    @Test
    void databaseEnforcesUniqueCpfAndEmail() {
        repository.saveAndFlush(customer("52998224725", "a@example.com"));

        assertThatThrownBy(() -> repository.saveAndFlush(customer("52998224725", "b@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseEnforcesUniqueEmail() {
        repository.saveAndFlush(customer("52998224725", "a@example.com"));

        assertThatThrownBy(() -> repository.saveAndFlush(customer("11144477735", "a@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void softDeletedCustomersAreExcluded() {
        Customer kept = repository.save(customer("52998224725", "a@example.com"));
        Customer deleted = customer("11144477735", "b@example.com");
        deleted.setDeletedAt(LocalDateTime.now());
        repository.saveAndFlush(deleted);

        assertThat(repository.findAllByDeletedAtIsNull(PageRequest.of(0, 10)).getContent())
                .extracting(Customer::getId).containsExactly(kept.getId());
        assertThat(repository.findByIdAndDeletedAtIsNull(deleted.getId())).isEmpty();
    }
}
