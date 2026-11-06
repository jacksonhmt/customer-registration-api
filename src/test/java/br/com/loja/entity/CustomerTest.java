package br.com.loja.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CustomerTest {

    @Test
    void maintainsBothSidesWhenReplacingAndRemovingAddress() {
        Customer customer = new Customer();
        Address first = new Address();
        Address second = new Address();

        customer.setAddress(first);
        assertThat(first.getCustomer()).isSameAs(customer);

        customer.setAddress(second);
        assertThat(first.getCustomer()).isNull();
        assertThat(second.getCustomer()).isSameAs(customer);
        assertThat(customer.getAddress()).isSameAs(second);

        customer.setAddress(null);
        assertThat(second.getCustomer()).isNull();
        assertThat(customer.getAddress()).isNull();
    }

    @Test
    void keepsManagedFieldsAndAddressOwnerSetterRestricted() throws NoSuchMethodException {
        assertThatThrownBy(() -> Customer.class.getMethod("setId", Long.class))
                .isInstanceOf(NoSuchMethodException.class);
        assertThatThrownBy(() -> Customer.class.getMethod("setCreatedAt", LocalDateTime.class))
                .isInstanceOf(NoSuchMethodException.class);
        assertThatThrownBy(() -> Customer.class.getMethod("setUpdatedAt", LocalDateTime.class))
                .isInstanceOf(NoSuchMethodException.class);
        assertThatThrownBy(() -> Address.class.getMethod("setId", Long.class))
                .isInstanceOf(NoSuchMethodException.class);
        assertThat(Address.class.getDeclaredMethod("setCustomer", Customer.class).getModifiers())
                .satisfies(modifiers -> {
                    assertThat(Modifier.isPublic(modifiers)).isFalse();
                    assertThat(Modifier.isProtected(modifiers)).isFalse();
                    assertThat(Modifier.isPrivate(modifiers)).isFalse();
                });
    }
}
