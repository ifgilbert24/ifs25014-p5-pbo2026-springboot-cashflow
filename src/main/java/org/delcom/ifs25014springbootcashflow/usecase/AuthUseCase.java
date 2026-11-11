package org.delcom.ifs25014springbootcashflow.usecase;

import org.delcom.ifs25014springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25014springbootcashflow.domain.entity.Role;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25014springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

public class AuthUseCase {

    private final IUserRepository userRepository;

    public AuthUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password, String fullName) throws ValidationException {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username tidak boleh kosong.");
        }
        if (password == null || password.length() < 4) {
            throw new ValidationException("Kata sandi minimal 4 karakter.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong.");
        }
        String normalizedUsername = username.trim().toLowerCase();
        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new ValidationException("Username sudah terdaftar.");
        }
        return userRepository.saveNew(
                normalizedUsername,
                PasswordHasher.hash(password),
                fullName.trim(),
                Role.USER
        );
    }

    public User login(String username, String password) throws ValidationException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ValidationException("Username dan kata sandi wajib diisi.");
        }
        String normalizedUsername = username.trim().toLowerCase();
        Optional<User> candidate = userRepository.findByUsername(normalizedUsername);
        if (candidate.isEmpty() || !PasswordHasher.matches(password, candidate.get().getPasswordHash())) {
            throw new ValidationException("Username atau kata sandi salah.");
        }
        return candidate.get();
    }

    public User ensureRootAdmin(String username, String password) throws ValidationException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ValidationException("Root admin tidak valid.");
        }
        String normalizedUsername = username.trim().toLowerCase();
        Optional<User> existing = userRepository.findByUsername(normalizedUsername);
        if (existing.isPresent()) {
            return existing.get();
        }
        return userRepository.saveNew(
                normalizedUsername,
                PasswordHasher.hash(password),
                "Administrator",
                Role.ADMIN
        );
    }
}

