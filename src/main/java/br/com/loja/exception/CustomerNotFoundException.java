package br.com.loja.exception;

import br.com.loja.message.CustomerMessages;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException() {
        super(CustomerMessages.NOT_FOUND);
    }
}
