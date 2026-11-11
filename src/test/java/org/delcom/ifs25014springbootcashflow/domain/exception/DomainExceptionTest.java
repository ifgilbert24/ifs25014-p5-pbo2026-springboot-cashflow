package org.delcom.ifs25014springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DomainExceptionTest {

    @Test
    void messageOnlyConstructor() {
        DomainException exception = new DomainException("pesan");
        assertEquals("pesan", exception.getMessage());
    }

    @Test
    void messageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException("akar");
        DomainException exception = new DomainException("pesan", cause);

        assertEquals("pesan", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void entityNotFoundKeepsId() {
        EntityNotFoundException exception = new EntityNotFoundException(42);
        assertEquals(42, exception.getId());
        assertEquals("Transaksi dengan ID 42 tidak ditemukan.", exception.getMessage());
    }

    @Test
    void validationExceptionKeepsMessage() {
        ValidationException exception = new ValidationException("keterangan kosong");
        assertEquals("keterangan kosong", exception.getMessage());
    }
}

