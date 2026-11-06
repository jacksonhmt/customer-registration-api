package br.com.loja.exception;

import lombok.Getter;

public class CustomerAlreadyExistsException extends RuntimeException {

    @Getter
    private final String field;

    public CustomerAlreadyExistsException(String field, String message) {
        super(message);
        this.field = field;
    }
}
