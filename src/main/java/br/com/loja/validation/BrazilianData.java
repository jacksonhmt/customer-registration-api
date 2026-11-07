package br.com.loja.validation;

import java.util.regex.Pattern;

public final class BrazilianData {

    private static final Pattern NON_DIGIT = Pattern.compile("\\D");
    private static final Pattern CPF_FORMAT = Pattern.compile("[0-9.\\-]+");
    private static final Pattern ZIP_CODE_FORMAT = Pattern.compile("\\d{8}|\\d{5}-\\d{3}");
    private static final Pattern PHONE_FORMAT = Pattern.compile("[0-9()\\s+\\-]+");
    private static final String VALID_DDDS =
            "11 12 13 14 15 16 17 18 19 21 22 24 27 28 31 32 33 34 35 37 38 41 42 43 44 45 46 47 48 49 "
                    + "51 53 54 55 61 62 63 64 65 66 67 68 69 71 73 74 75 77 79 81 82 83 84 85 86 87 88 89 "
                    + "91 92 93 94 95 96 97 98 99";

    private BrazilianData() {
    }

    public static String digitsOnly(String value) {
        return value == null ? null : NON_DIGIT.matcher(value).replaceAll("");
    }

    public static boolean isValidCpf(String value) {
        if (value == null || !CPF_FORMAT.matcher(value).matches()) {
            return false;
        }
        String cpf = digitsOnly(value);
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return checkDigit(cpf, 9) == cpf.charAt(9) - '0' && checkDigit(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int checkDigit(String cpf, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (cpf.charAt(i) - '0') * (length + 1 - i);
        }
        int rest = (sum * 10) % 11;
        return rest == 10 ? 0 : rest;
    }

    public static boolean isValidZipCode(String value) {
        return value != null && ZIP_CODE_FORMAT.matcher(value).matches();
    }

    /** Accepts landline (DDD + 8 digits starting 2-5) and mobile (DDD + 9 + 8 digits), with optional +55. */
    public static boolean isValidPhone(String value) {
        if (value == null || !PHONE_FORMAT.matcher(value).matches()) {
            return false;
        }
        return normalizePhone(value) != null;
    }

    /** Returns digits-only phone without country code, or null when invalid. */
    public static String normalizePhone(String value) {
        if (value == null) {
            return null;
        }
        String digits = digitsOnly(value);
        if (digits.startsWith("55") && (digits.length() == 12 || digits.length() == 13)) {
            digits = digits.substring(2);
        }
        if (digits.length() != 10 && digits.length() != 11) {
            return null;
        }
        if (!VALID_DDDS.contains(digits.substring(0, 2))) {
            return null;
        }
        String local = digits.substring(2);
        boolean mobile = local.length() == 9 && local.charAt(0) == '9';
        boolean landline = local.length() == 8 && local.charAt(0) >= '2' && local.charAt(0) <= '5';
        return mobile || landline ? digits : null;
    }
}
