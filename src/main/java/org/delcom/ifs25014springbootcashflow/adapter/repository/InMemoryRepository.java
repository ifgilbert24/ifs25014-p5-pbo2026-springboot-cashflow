package org.delcom.ifs25014springbootcashflow.adapter.repository;

import org.delcom.ifs25014springbootcashflow.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class InMemoryRepository<T, ID> implements IRepository<T, ID> {

    protected final List<T> storage = new ArrayList<>();
    private final Function<T, ID> idExtractor;

    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public T save(T entity) {
        ID entityId = idExtractor.apply(entity);
        for (int i = 0; i < storage.size(); i++) {
            if (idExtractor.apply(storage.get(i)).equals(entityId)) {
                storage.set(i, entity);
                return entity;
            }
        }
        storage.add(entity);
        return entity;
    }

    @Override
    public boolean deleteById(ID id) {
        return storage.removeIf(item -> idExtractor.apply(item).equals(id));
    }

    @Override
    public Optional<T> findById(ID id) {
        return storage.stream()
                .filter(item -> idExtractor.apply(item).equals(id))
                .findFirst();
    }

    @Override
    public List<T> findAll() {
        return Collections.unmodifiableList(storage);
    }
}

