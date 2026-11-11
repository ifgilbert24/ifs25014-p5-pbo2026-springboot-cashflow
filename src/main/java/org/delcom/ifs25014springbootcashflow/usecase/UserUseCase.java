package org.delcom.ifs25014springbootcashflow.usecase;

import org.delcom.ifs25014springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25014springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25014springbootcashflow.domain.repository.IUserRepository;

import java.util.List;

public class UserUseCase {

    private final IUserRepository userRepository;

    public UserUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User getUser(int id) throws EntityNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
    }

    public User updateProfile(int userId, String fullName, String newPassword)
            throws EntityNotFoundException, ValidationException {
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong.");
        }
        User user = getUser(userId);
        user.changeFullName(fullName.trim());
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 4) {
                throw new ValidationException("Kata sandi baru minimal 4 karakter.");
            }
            user.changePasswordHash(PasswordHasher.hash(newPassword));
        }
        return userRepository.save(user);
    }

    public User updatePhoto(int userId, String photoPath)
            throws EntityNotFoundException, ValidationException {
        if (photoPath == null || photoPath.isBlank()) {
            throw new ValidationException("Path foto tidak valid.");
        }
        User user = getUser(userId);
        user.changePhotoPath(photoPath);
        return userRepository.save(user);
    }
}

