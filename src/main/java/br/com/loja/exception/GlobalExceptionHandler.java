package br.com.loja.exception;

import br.com.loja.dto.response.ApiErrorResponseDTO;
import br.com.loja.enums.ErrorCode;
import br.com.loja.dto.response.FieldErrorDTO;
import br.com.loja.message.CustomerMessages;
import br.com.loja.message.GeneralMessages;
import br.com.loja.message.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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

    private final MessageService messages;

    public GlobalExceptionHandler(MessageService messages) {
        this.messages = messages;
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    ResponseEntity<ApiErrorResponseDTO> notFound(HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.CUSTOMER_NOT_FOUND, request, null);
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    ResponseEntity<ApiErrorResponseDTO> conflict(CustomerAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ErrorCode.CUSTOMER_ALREADY_EXISTS, request,
                List.of(new FieldErrorDTO(ex.getField(), messages.getMessage(ex.getMessageKey()))));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorResponseDTO> integrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String detail = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase();
        if (detail.contains("cpf")) {
            return conflict(new CustomerAlreadyExistsException("cpf", CustomerMessages.CPF_ALREADY_REGISTERED), request);
        }
        if (detail.contains("email")) {
            return conflict(new CustomerAlreadyExistsException("email", CustomerMessages.EMAIL_ALREADY_REGISTERED),
                    request);
        }
        log.error("Data integrity violation", ex);
        return internal(request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponseDTO> invalidBody(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDTO> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldErrorDTO(e.getField(), e.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, request, errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiErrorResponseDTO> invalidParameters(HandlerMethodValidationException ex,
                                                          HttpServletRequest request) {
        List<FieldErrorDTO> errors = ex.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                        .map(e -> new FieldErrorDTO(r.getMethodParameter().getParameterName(), e.getDefaultMessage())))
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiErrorResponseDTO> constraintViolation(ConstraintViolationException ex,
                                                            HttpServletRequest request) {
        List<FieldErrorDTO> errors = ex.getConstraintViolations().stream()
                .map(v -> new FieldErrorDTO(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponseDTO> unreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String field = null;
        String messageKey = GeneralMessages.MALFORMED_BODY;
        Object[] args = {};
        if (ex.getCause() instanceof MismatchedInputException mie) {
            field = mie.getPath().stream()
                    .map(JacksonException.Reference::getPropertyName)
                    .filter(Objects::nonNull)
                    .reduce((a, b) -> a + "." + b).orElse(null);
            if (mie instanceof InvalidFormatException ife && ife.getTargetType() != null) {
                Class<?> type = ife.getTargetType();
                if (Temporal.class.isAssignableFrom(type)) {
                    messageKey = GeneralMessages.INVALID_DATE_FORMAT;
                } else if (type.isEnum()) {
                    messageKey = GeneralMessages.INVALID_ENUM_VALUE;
                    args = new Object[]{Arrays.toString(type.getEnumConstants())};
                } else {
                    messageKey = GeneralMessages.INVALID_FIELD_VALUE;
                }
            } else if (field != null) {
                messageKey = GeneralMessages.INVALID_FIELD_VALUE;
            }
        }
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, request,
                List.of(new FieldErrorDTO(field, messages.getMessage(messageKey, args))));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiErrorResponseDTO> typeMismatch(MethodArgumentTypeMismatchException ex,
                                                     HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, request,
                List.of(new FieldErrorDTO(ex.getName(), messages.getMessage(GeneralMessages.INVALID_PARAMETER_VALUE))));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiErrorResponseDTO> noResource(HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiErrorResponseDTO> methodNotAllowed(HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED, request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiErrorResponseDTO> unsupportedMediaType(HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCode.UNSUPPORTED_MEDIA_TYPE, request, null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponseDTO> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return internal(request);
    }

    private ResponseEntity<ApiErrorResponseDTO> internal(HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, request, null);
    }

    private ResponseEntity<ApiErrorResponseDTO> build(HttpStatus status, ErrorCode code, HttpServletRequest request,
                                                      List<FieldErrorDTO> errors) {
        ApiErrorResponseDTO body = ApiErrorResponseDTO.of(code.name(), messages.getMessage(code.messageKey()),
                request.getRequestURI(), errors);
        return ResponseEntity.status(status).body(body);
    }
}
