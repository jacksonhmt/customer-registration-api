package br.com.loja.service.impl;

import br.com.loja.dto.request.CustomerRequestDTO;
import br.com.loja.dto.response.CustomerResponseDTO;
import br.com.loja.entity.Customer;
import br.com.loja.exception.CustomerAlreadyExistsException;
import br.com.loja.exception.CustomerNotFoundException;
import br.com.loja.message.CustomerMessages;
import br.com.loja.repository.CustomerRepository;
import br.com.loja.service.CustomerMapper;
import br.com.loja.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public CustomerServiceImpl(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CustomerResponseDTO create(CustomerRequestDTO request) {
        String cpf = CustomerMapper.normalizeCpf(request.cpf());
        String email = CustomerMapper.normalizeEmail(request.email());
        if (repository.existsByCpf(cpf)) {
            throw new CustomerAlreadyExistsException("cpf", CustomerMessages.CPF_ALREADY_REGISTERED);
        }
        if (repository.existsByEmail(email)) {
            throw new CustomerAlreadyExistsException("email", CustomerMessages.EMAIL_ALREADY_REGISTERED);
        }
        Customer customer = new Customer();
        mapper.apply(request, customer);
        return mapper.toResponse(repository.saveAndFlush(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDTO findById(Long id) {
        return mapper.toResponse(getActive(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponseDTO> findAll(Pageable pageable) {
        return repository.findAllByDeletedAtIsNull(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CustomerResponseDTO update(Long id, CustomerRequestDTO request) {
        Customer customer = getActive(id);
        String cpf = CustomerMapper.normalizeCpf(request.cpf());
        String email = CustomerMapper.normalizeEmail(request.email());
        if (repository.existsByCpfAndIdNot(cpf, id)) {
            throw new CustomerAlreadyExistsException("cpf", CustomerMessages.CPF_ALREADY_REGISTERED);
        }
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new CustomerAlreadyExistsException("email", CustomerMessages.EMAIL_ALREADY_REGISTERED);
        }
        mapper.apply(request, customer);
        return mapper.toResponse(repository.saveAndFlush(customer));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = getActive(id);
        repository.delete(customer);
        repository.flush();
    }

    private Customer getActive(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id).orElseThrow(CustomerNotFoundException::new);
    }
}
