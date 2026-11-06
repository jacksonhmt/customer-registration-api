package br.com.loja.service.impl;

import br.com.loja.dto.request.CustomerRequest;
import br.com.loja.dto.response.CustomerResponse;
import br.com.loja.entity.Customer;
import br.com.loja.exception.CustomerAlreadyExistsException;
import br.com.loja.exception.CustomerNotFoundException;
import br.com.loja.repository.CustomerRepository;
import br.com.loja.service.CustomerMapper;
import br.com.loja.service.CustomerService;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String CPF_EXISTS = "A customer with this CPF is already registered.";
    private static final String EMAIL_EXISTS = "A customer with this email is already registered.";

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public CustomerServiceImpl(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String cpf = CustomerMapper.normalizeCpf(request.cpf());
        String email = CustomerMapper.normalizeEmail(request.email());
        if (repository.existsByCpf(cpf)) {
            throw new CustomerAlreadyExistsException("cpf", CPF_EXISTS);
        }
        if (repository.existsByEmail(email)) {
            throw new CustomerAlreadyExistsException("email", EMAIL_EXISTS);
        }
        Customer customer = new Customer();
        mapper.apply(request, customer);
        return mapper.toResponse(repository.saveAndFlush(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return mapper.toResponse(getActive(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAll(Pageable pageable) {
        return repository.findAllByDeletedAtIsNull(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = getActive(id);
        String cpf = CustomerMapper.normalizeCpf(request.cpf());
        String email = CustomerMapper.normalizeEmail(request.email());
        if (repository.existsByCpfAndIdNot(cpf, id)) {
            throw new CustomerAlreadyExistsException("cpf", CPF_EXISTS);
        }
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new CustomerAlreadyExistsException("email", EMAIL_EXISTS);
        }
        mapper.apply(request, customer);
        return mapper.toResponse(repository.saveAndFlush(customer));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = getActive(id);
        customer.setDeletedAt(LocalDateTime.now());
        repository.save(customer);
    }

    private Customer getActive(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id).orElseThrow(CustomerNotFoundException::new);
    }
}
