package br.com.loja.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.loja.dto.request.AddressRequest;
import br.com.loja.dto.request.CustomerRequest;
import br.com.loja.dto.response.CustomerResponse;
import br.com.loja.entity.Customer;
import br.com.loja.exception.CustomerAlreadyExistsException;
import br.com.loja.exception.CustomerNotFoundException;
import br.com.loja.repository.CustomerRepository;
import br.com.loja.service.impl.CustomerServiceImpl;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    CustomerRepository repository;

    CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(repository, new CustomerMapper());
    }

    private static CustomerRequest request() {
        return new CustomerRequest("Maria da Silva", "529.982.247-25", " Maria@Example.com ", "(27) 99999-9999",
                null, LocalDate.of(1990, 5, 15), null,
                new AddressRequest("29101-000", "Avenida Principal", "100", null, "Centro", "Vila Velha", "es"));
    }

    @Test
    void createNormalizesDataAndPersists() {
        when(repository.saveAndFlush(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        CustomerResponse response = service.create(request());

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repository).saveAndFlush(captor.capture());
        Customer saved = captor.getValue();
        assertThat(saved.getCpf()).isEqualTo("52998224725");
        assertThat(saved.getEmail()).isEqualTo("maria@example.com");
        assertThat(saved.getPhoneNumber()).isEqualTo("27999999999");
        assertThat(saved.getAddress().getZipCode()).isEqualTo("29101000");
        assertThat(saved.getAddress().getState()).isEqualTo("ES");
        assertThat(saved.getAddress().getCustomer()).isSameAs(saved);
        assertThat(response.fullName()).isEqualTo("Maria da Silva");
    }

    @Test
    void createRejectsDuplicateCpf() {
        when(repository.existsByCpf("52998224725")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(CustomerAlreadyExistsException.class)
                .extracting("field").isEqualTo("cpf");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(repository.existsByEmail("maria@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(CustomerAlreadyExistsException.class)
                .extracting("field").isEqualTo("email");
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findByIdAndDeletedAtIsNull(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(9L)).isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void updateReplacesFieldsAndRemovesAddressWhenOmitted() {
        Customer existing = new Customer();
        new CustomerMapper().apply(request(), existing);
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(existing)).thenReturn(existing);

        var update = new CustomerRequest("Maria Souza", "52998224725", "maria@example.com", "2732221111",
                null, LocalDate.of(1990, 5, 15), null, null);
        CustomerResponse response = service.update(1L, update);

        assertThat(response.fullName()).isEqualTo("Maria Souza");
        assertThat(response.address()).isNull();
        assertThat(existing.getPhoneNumber()).isEqualTo("2732221111");
    }

    @Test
    void updateRejectsCpfOwnedByAnotherCustomer() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(new Customer()));
        when(repository.existsByCpfAndIdNot("52998224725", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request())).isInstanceOf(CustomerAlreadyExistsException.class);
    }

    @Test
    void updateThrowsWhenMissing() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, request())).isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void deleteIsPhysical() {
        Customer existing = new Customer();
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repository).delete(existing);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(CustomerNotFoundException.class);
    }
}
