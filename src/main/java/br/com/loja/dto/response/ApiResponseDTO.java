package br.com.loja.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponseDTO<T>(boolean success, String message, T data) {

    public static <T> ApiResponseDTO<T> ok(String message, T data) {
        return new ApiResponseDTO<>(true, message, data);
    }

    public static <T> ApiResponseDTO<T> ok(String message) {
        return new ApiResponseDTO<>(true, message, null);
    }
}
