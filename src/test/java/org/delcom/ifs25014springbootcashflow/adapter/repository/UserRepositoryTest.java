package org.delcom.ifs25014springbootcashflow.adapter.repository;

import org.delcom.ifs25014springbootcashflow.domain.entity.Role;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest {

    @Test
    void saveNewFindAndUpdateInMemory() {
        UserRepository repo = new UserRepository();
        User u = repo.saveNew("andi", "h", "Andi", Role.USER);
        assertEquals(1, u.getId());

        assertTrue(repo.findByUsername("ANDI").isPresent());
        assertTrue(repo.findByUsername("tidakada").isEmpty());

        u.changeFullName("Andi Update");
        repo.save(u);
        assertEquals("Andi Update", repo.findById(1).orElseThrow().getFullName());
    }
}

