package br.com.loja.exception;

import lombok.Getter;

public class CustomerAlreadyExistsException extends RuntimeException {

    @Getter
    private final String field;

    @Getter
    private final String messageKey;

    public CustomerAlreadyExistsException(String field, String messageKey) {
        super(messageKey);
        this.field = field;
        this.messageKey = messageKey;
    }
}
