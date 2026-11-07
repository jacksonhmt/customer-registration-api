package br.com.loja.enums;

import br.com.loja.message.CustomerMessages;
import br.com.loja.message.GeneralMessages;

/** Stable, language-independent error codes and the message key used to describe each of them. */
public enum ErrorCode {
    CUSTOMER_NOT_FOUND(CustomerMessages.NOT_FOUND),
    CUSTOMER_ALREADY_EXISTS(CustomerMessages.ALREADY_EXISTS),
    VALIDATION_ERROR(GeneralMessages.VALIDATION_ERROR),
    INVALID_REQUEST(GeneralMessages.INVALID_REQUEST),
    RESOURCE_NOT_FOUND(GeneralMessages.RESOURCE_NOT_FOUND),
    METHOD_NOT_ALLOWED(GeneralMessages.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE(GeneralMessages.UNSUPPORTED_MEDIA_TYPE),
    INTERNAL_SERVER_ERROR(GeneralMessages.INTERNAL_ERROR);

    private final String messageKey;

    ErrorCode(String messageKey) {
        this.messageKey = messageKey;
    }

    public String messageKey() {
        return messageKey;
    }
}
