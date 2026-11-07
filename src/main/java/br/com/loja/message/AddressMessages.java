package br.com.loja.message;

/** Address Bean Validation placeholders resolved by the MessageSource. */
public final class AddressMessages {

    public static final String ZIP_CODE_REQUIRED = "{address.zip-code.required}";
    public static final String ZIP_CODE_INVALID = "{address.invalid-zip-code}";
    public static final String STREET_REQUIRED = "{address.street.required}";
    public static final String STREET_SIZE = "{address.street.size}";
    public static final String NUMBER_REQUIRED = "{address.number.required}";
    public static final String NUMBER_SIZE = "{address.number.size}";
    public static final String COMPLEMENT_SIZE = "{address.complement.size}";
    public static final String NEIGHBORHOOD_REQUIRED = "{address.neighborhood.required}";
    public static final String NEIGHBORHOOD_SIZE = "{address.neighborhood.size}";
    public static final String CITY_REQUIRED = "{address.city.required}";
    public static final String CITY_SIZE = "{address.city.size}";
    public static final String STATE_REQUIRED = "{address.state.required}";
    public static final String STATE_INVALID = "{address.state.invalid}";

    private AddressMessages() {
    }
}
