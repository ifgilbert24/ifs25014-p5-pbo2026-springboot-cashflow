package org.delcom.ifs25014springbootcashflow.adapter.repository;

import org.delcom.ifs25014springbootcashflow.domain.entity.Role;
import org.delcom.ifs25014springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcRepositoryTest {

    private JdbcUserRepository userRepository;
    private JdbcTransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test_" + System.nanoTime() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        SchemaInitializer.initialize(dataSource);
        userRepository = new JdbcUserRepository(dataSource);
        transactionRepository = new JdbcTransactionRepository(dataSource);
    }

    @Test
    void userCrudFlow() {
        User created = userRepository.saveNew("andi", "hash1", "Andi", Role.USER);
        assertTrue(created.getId() > 0);
        assertNull(created.getPhotoPath());

        assertTrue(userRepository.findById(created.getId()).isPresent());
        assertTrue(userRepository.findById(999).isEmpty());
        assertTrue(userRepository.findByUsername("ANDI").isPresent());
        assertTrue(userRepository.findByUsername("tidakada").isEmpty());

        created.changeFullName("Andi Wijaya");
        created.changePasswordHash("hash2");
        created.changePhotoPath("profiles/a.png");
        userRepository.save(created);

        User reloaded = userRepository.findById(created.getId()).orElseThrow();
        assertEquals("Andi Wijaya", reloaded.getFullName());
        assertEquals("hash2", reloaded.getPasswordHash());
        assertEquals("profiles/a.png", reloaded.getPhotoPath());

        assertEquals(1, userRepository.findAll().size());
        assertTrue(userRepository.deleteById(created.getId()));
        assertFalse(userRepository.deleteById(created.getId()));
    }

    @Test
    void transactionCrudFlow() {
        User u1 = userRepository.saveNew("u1", "h", "User 1", Role.USER);
        User u2 = userRepository.saveNew("u2", "h", "User 2", Role.USER);

        Transaction t1 = transactionRepository.save(u1.getId(), "Gaji", 5_000_000L, TransactionType.INCOME);
        assertTrue(t1.getId() > 0);

        t1.changeDescription("Gaji Pokok");
        t1.changeAmount(5_500_000L);
        t1.changeType(TransactionType.INCOME);
        t1.changeProofPath("proofs/slip.png");
        transactionRepository.save(t1);

        Transaction loaded = transactionRepository.findById(t1.getId()).orElseThrow();
        assertEquals("Gaji Pokok", loaded.getDescription());
        assertEquals(5_500_000L, loaded.getAmount());
        assertEquals("proofs/slip.png", loaded.getProofPath());
        assertNotNull(loaded.getCreatedAt());

        t1.changeProofPath(null);
        transactionRepository.save(t1);
        Transaction cleared = transactionRepository.findById(t1.getId()).orElseThrow();
        assertNull(cleared.getProofPath());

        Transaction manual = new Transaction(
                500,
                u2.getId(),
                "Manual",
                75_000L,
                TransactionType.EXPENSE,
                "proofs/m.png",
                LocalDateTime.of(2026, 11, 1, 9, 0)
        );
        transactionRepository.save(manual);
        assertTrue(transactionRepository.findById(500).isPresent());

        Transaction manualNoProof = new Transaction(
                501,
                u2.getId(),
                "Manual2",
                20_000L,
                TransactionType.INCOME,
                null,
                LocalDateTime.of(2026, 11, 1, 9, 0)
        );
        transactionRepository.save(manualNoProof);
        assertTrue(transactionRepository.findById(501).isPresent());
        assertTrue(transactionRepository.findById(9999).isEmpty());

        List<Transaction> u1List = transactionRepository.findByUserId(u1.getId());
        assertEquals(1, u1List.size());
        assertEquals(3, transactionRepository.findAll().size());

        assertTrue(transactionRepository.deleteById(t1.getId()));
        assertFalse(transactionRepository.deleteById(t1.getId()));
    }
}

