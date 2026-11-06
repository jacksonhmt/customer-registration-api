package br.com.loja.service;

import br.com.loja.dto.request.CustomerRequest;
import br.com.loja.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse findById(Long id);

    Page<CustomerResponse> findAll(Pageable pageable);

    CustomerResponse update(Long id, CustomerRequest request);

    /** Logical deletion: the record is kept so future sales can still reference it. */
    void delete(Long id);
}
