package org.delcom.ifs25014springbootcashflow.domain.exception;

public class EntityNotFoundException extends DomainException {

    private final int id;

    public EntityNotFoundException(int id) {
        super("Transaksi dengan ID " + id + " tidak ditemukan.");
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

