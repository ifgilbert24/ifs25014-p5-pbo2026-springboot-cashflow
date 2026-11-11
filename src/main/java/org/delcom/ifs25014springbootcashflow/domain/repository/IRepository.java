package org.delcom.ifs25014springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;

public interface IRepository<T, ID> {

    T save(T entity);

    boolean deleteById(ID id);

    Optional<T> findById(ID id);

    List<T> findAll();
}

