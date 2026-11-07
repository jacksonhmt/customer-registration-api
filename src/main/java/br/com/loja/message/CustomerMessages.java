package br.com.loja.message;

/** Customer message keys. Keys in braces are Bean Validation placeholders resolved by the MessageSource. */
public final class CustomerMessages {

    public static final String CREATED = "customer.created";
    public static final String LIST_RETRIEVED = "customer.list-retrieved";
    public static final String RETRIEVED = "customer.retrieved";
    public static final String UPDATED = "customer.updated";
    public static final String DELETED = "customer.deleted";
    public static final String NOT_FOUND = "customer.not-found";
    public static final String ALREADY_EXISTS = "customer.already-exists";
    public static final String CPF_ALREADY_REGISTERED = "customer.cpf.already-exists";
    public static final String EMAIL_ALREADY_REGISTERED = "customer.email.already-exists";

    public static final String FULL_NAME_REQUIRED = "{customer.name.required}";
    public static final String FULL_NAME_SIZE = "{customer.name.size}";
    public static final String CPF_REQUIRED = "{customer.cpf.required}";
    public static final String CPF_INVALID = "{customer.invalid-cpf}";
    public static final String EMAIL_REQUIRED = "{customer.email.required}";
    public static final String EMAIL_INVALID = "{customer.invalid-email}";
    public static final String EMAIL_SIZE = "{customer.email.size}";
    public static final String PHONE_REQUIRED = "{customer.phone.required}";
    public static final String PHONE_INVALID = "{customer.invalid-phone}";
    public static final String BIRTH_DATE_REQUIRED = "{customer.birth-date.required}";
    public static final String BIRTH_DATE_NOT_FUTURE = "{customer.birth-date.future}";
    public static final String PAGE_MINIMUM = "{general.page.min}";
    public static final String PAGE_SIZE = "{general.page.size}";

    private CustomerMessages() {
    }
}
