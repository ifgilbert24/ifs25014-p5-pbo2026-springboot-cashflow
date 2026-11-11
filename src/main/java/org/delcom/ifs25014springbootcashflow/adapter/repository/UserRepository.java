package org.delcom.ifs25014springbootcashflow.adapter.repository;

import org.delcom.ifs25014springbootcashflow.domain.entity.Role;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.delcom.ifs25014springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

public class UserRepository extends InMemoryRepository<User, Integer> implements IUserRepository {

    private int nextId = 1;

    public UserRepository() {
        super(User::getId);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return storage.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public User saveNew(String username, String passwordHash, String fullName, Role role) {
        User user = new User(nextId++, username, passwordHash, fullName, role, null);
        return save(user);
    }
}

