package br.com.loja.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BrazilianDataTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "529.982.247-25", "11144477735"})
    void acceptsValidCpf(String cpf) {
        assertThat(BrazilianData.isValidCpf(cpf)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"52998224724", "11111111111", "1234567890", "529982247255", "abc", "529 982 247 25", ""})
    void rejectsInvalidCpf(String cpf) {
        assertThat(BrazilianData.isValidCpf(cpf)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"27999999999", "(27) 99999-9999", "2732221111", "+55 27 99999-9999", "5527999999999"})
    void acceptsValidPhone(String phone) {
        assertThat(BrazilianData.isValidPhone(phone)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0999999999", "10999999999", "2799999999", "27899999999", "2791111111", "99999", "27a99999999"})
    void rejectsInvalidPhone(String phone) {
        assertThat(BrazilianData.isValidPhone(phone)).isFalse();
    }

    @org.junit.jupiter.api.Test
    void normalizesPhoneToDigitsWithoutCountryCode() {
        assertThat(BrazilianData.normalizePhone("+55 (27) 99999-9999")).isEqualTo("27999999999");
    }

    @org.junit.jupiter.api.Test
    void normalizesNullPhoneWithoutThrowing() {
        assertThat(BrazilianData.normalizePhone(null)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"29101000", "29101-000"})
    void acceptsValidZipCode(String zip) {
        assertThat(BrazilianData.isValidZipCode(zip)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2910100", "291010000", "2910-1000", "abcdefgh"})
    void rejectsInvalidZipCode(String zip) {
        assertThat(BrazilianData.isValidZipCode(zip)).isFalse();
    }
}
