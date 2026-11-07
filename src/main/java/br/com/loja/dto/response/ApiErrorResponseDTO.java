package br.com.loja.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** Error envelope. {@code errors} is present only when field-level details are available. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponseDTO(boolean success, String code, String message, String path,
                                  List<FieldErrorDTO> errors) {

    public static ApiErrorResponseDTO of(String code, String message, String path, List<FieldErrorDTO> errors) {
        return new ApiErrorResponseDTO(false, code, message, path, errors);
    }
}
