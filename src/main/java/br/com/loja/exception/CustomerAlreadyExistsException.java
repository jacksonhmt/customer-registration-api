package br.com.loja.exception;

public class CustomerAlreadyExistsException extends RuntimeException {

    private final String field;

    public CustomerAlreadyExistsException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
