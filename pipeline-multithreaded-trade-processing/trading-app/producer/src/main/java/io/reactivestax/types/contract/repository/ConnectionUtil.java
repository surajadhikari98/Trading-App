package io.reactivestax.types.contract.repository;

import java.io.FileNotFoundException;

public interface ConnectionUtil<T> {
    T getConnection() throws FileNotFoundException;
}
