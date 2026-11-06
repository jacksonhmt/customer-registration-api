package br.com.loja.exception;

import br.com.loja.dto.response.ApiError;
import br.com.loja.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomerNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> notFound(CustomerNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Customer not found.",
                new ApiError("id", "No customer was found with the provided ID."));
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    ResponseEntity<ApiResponse<Void>> conflict(CustomerAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, "Customer already exists.", new ApiError(ex.getField(), ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<Void>> integrity(DataIntegrityViolationException ex) {
        String detail = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase();
        if (detail.contains("cpf")) {
            return conflict(new CustomerAlreadyExistsException("cpf",
                    "A customer with this CPF is already registered."));
        }
        if (detail.contains("email")) {
            return conflict(new CustomerAlreadyExistsException("email",
                    "A customer with this email is already registered."));
        }
        log.error("Data integrity violation", ex);
        return internal();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> invalidBody(MethodArgumentNotValidException ex) {
        List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiError(e.getField(), e.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation error.", errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiResponse<Void>> invalidParameters(HandlerMethodValidationException ex) {
        List<ApiError> errors = ex.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                        .map(e -> new ApiError(r.getMethodParameter().getParameterName(), e.getDefaultMessage())))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation error.", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiResponse<Void>> constraintViolation(ConstraintViolationException ex) {
        List<ApiError> errors = ex.getConstraintViolations().stream()
                .map(v -> new ApiError(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation error.", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> unreadable(HttpMessageNotReadableException ex) {
        String field = null;
        String message = "The request body is malformed.";
        if (ex.getCause() instanceof MismatchedInputException mie) {
            field = mie.getPath().stream()
                    .map(JacksonException.Reference::getPropertyName)
                    .filter(java.util.Objects::nonNull)
                    .reduce((a, b) -> a + "." + b).orElse(null);
            if (mie instanceof InvalidFormatException ife && ife.getTargetType() != null) {
                Class<?> type = ife.getTargetType();
                if (java.time.temporal.Temporal.class.isAssignableFrom(type)) {
                    message = "The date must follow the YYYY-MM-DD format.";
                } else if (type.isEnum()) {
                    message = "Invalid value. Accepted values: "
                            + java.util.Arrays.toString(type.getEnumConstants()) + ".";
                } else {
                    message = "Invalid value for this field.";
                }
            } else if (field != null) {
                message = "Invalid value for this field.";
            }
        }
        return build(HttpStatus.BAD_REQUEST, "Invalid request.", new ApiError(field, message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse<Void>> typeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid request.",
                new ApiError(ex.getName(), "Invalid value for this parameter."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiResponse<Void>> noResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Resource not found.", new ApiError(null, "The requested resource does not exist."));
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiResponse<Void>> methodNotAllowed(Exception ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Invalid request.",
                new ApiError(null, "HTTP method not supported for this endpoint."));
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiResponse<Void>> unsupportedMediaType(Exception ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Invalid request.",
                new ApiError(null, "Content type not supported. Use application/json."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> unexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return internal();
    }

    private ResponseEntity<ApiResponse<Void>> internal() {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.",
                new ApiError(null, "Please contact support if the problem persists."));
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message, ApiError error) {
        return build(status, message, List.of(error));
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message, List<ApiError> errors) {
        return ResponseEntity.status(status).body(ApiResponse.error(message, errors));
    }
}
