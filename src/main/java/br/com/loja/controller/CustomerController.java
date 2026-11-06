package br.com.loja.controller;

import br.com.loja.dto.request.CustomerRequest;
import br.com.loja.dto.response.ApiResponse;
import br.com.loja.dto.response.CustomerResponse;
import br.com.loja.dto.response.PageResponse;
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

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Register a customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Customer successfully registered.", service.create(request)));
    }

    @GetMapping
    @Operation(summary = "List customers (paginated)")
    public ApiResponse<PageResponse<CustomerResponse>> findAll(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or greater.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Size must be between 1 and 100.")
            @Max(value = 100, message = "Size must be between 1 and 100.") int size) {
        var result = service.findAll(PageRequest.of(page, size, Sort.by("id")));
        return ApiResponse.ok("Customers successfully retrieved.", PageResponse.from(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a customer by ID")
    public ApiResponse<CustomerResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok("Customer successfully retrieved.", service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a customer (complete update)")
    public ApiResponse<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return ApiResponse.ok("Customer successfully updated.", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer (soft delete)")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok("Customer successfully deleted.");
    }
}
