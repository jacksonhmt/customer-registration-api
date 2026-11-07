package br.com.loja.service;

import br.com.loja.dto.request.CustomerRequestDTO;
import br.com.loja.dto.response.CustomerResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponseDTO create(CustomerRequestDTO request);

    CustomerResponseDTO findById(Long id);

    Page<CustomerResponseDTO> findAll(Pageable pageable);

    CustomerResponseDTO update(Long id, CustomerRequestDTO request);

    /** Logical deletion: the record is kept so future sales can still reference it. */
    void delete(Long id);
}
