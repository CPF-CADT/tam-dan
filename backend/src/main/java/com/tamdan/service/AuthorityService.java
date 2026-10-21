package com.tamdan.service;

import com.tamdan.dto.request.AuthorityRequest;
import com.tamdan.dto.response.AuthorityResponse;
import com.tamdan.entities.Authority;
import com.tamdan.exception.ConflictException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.AuthorityRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final AuthorityRepository authorityRepository;

    @Transactional
    public AuthorityResponse create(AuthorityRequest request) {
        String name = normalizeName(request.name());
        if (authorityRepository.existsById(name)) throw new ConflictException("Authority already exists");
        try {
            return toResponse(authorityRepository.save(Authority.named(name)));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Authority already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<AuthorityResponse> findAll() {
        return authorityRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AuthorityResponse findOne(String name) {
        return toResponse(requireAuthority(name));
    }

    @Transactional
    public void delete(String name) {
        authorityRepository.delete(requireAuthority(name));
    }

    private Authority requireAuthority(String name) {
        return authorityRepository
            .findById(normalizeName(name))
            .orElseThrow(() -> new ResourceNotFoundException("Authority was not found"));
    }

    private AuthorityResponse toResponse(Authority authority) {
        return new AuthorityResponse(authority.getName());
    }

    private String normalizeName(String name) {
        return name.trim().toUpperCase(Locale.ENGLISH);
    }
}
