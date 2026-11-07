package br.com.loja.controller;

import br.com.loja.dto.request.CustomerRequestDTO;
import br.com.loja.dto.response.ApiResponseDTO;
import br.com.loja.dto.response.CustomerResponseDTO;
import br.com.loja.dto.response.PageResponseDTO;
import br.com.loja.message.CustomerMessages;
import br.com.loja.message.MessageService;
import br.com.loja.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@Validated
@Tag(name = "Customers")
public class CustomerController {

    private final CustomerService service;
    private final MessageService messages;

    public CustomerController(CustomerService service, MessageService messages) {
        this.service = service;
        this.messages = messages;
    }

    @PostMapping
    @Operation(summary = "Register a customer")
    public ResponseEntity<ApiResponseDTO<CustomerResponseDTO>> create(@Valid @RequestBody CustomerRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok(messages.getMessage(CustomerMessages.CREATED), service.create(request)));
    }

    @GetMapping
    @Operation(summary = "List customers (paginated)")
    public ApiResponseDTO<PageResponseDTO<CustomerResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = CustomerMessages.PAGE_MINIMUM) int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = CustomerMessages.PAGE_SIZE)
            @Max(value = 100, message = CustomerMessages.PAGE_SIZE) int size) {
        var result = service.findAll(PageRequest.of(page, size, Sort.by("id")));
        return ApiResponseDTO.ok(messages.getMessage(CustomerMessages.LIST_RETRIEVED), PageResponseDTO.from(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a customer by ID")
    public ApiResponseDTO<CustomerResponseDTO> findById(@PathVariable Long id) {
        return ApiResponseDTO.ok(messages.getMessage(CustomerMessages.RETRIEVED), service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a customer (complete update)")
    public ApiResponseDTO<CustomerResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CustomerRequestDTO request) {
        return ApiResponseDTO.ok(messages.getMessage(CustomerMessages.UPDATED), service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer")
    public ApiResponseDTO<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponseDTO.ok(messages.getMessage(CustomerMessages.DELETED));
    }
}
