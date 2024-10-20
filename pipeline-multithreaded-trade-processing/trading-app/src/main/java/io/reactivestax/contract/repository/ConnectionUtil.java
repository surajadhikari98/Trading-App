package io.reactivestax.contract.repository;

import java.io.FileNotFoundException;

public interface ConnectionUtil<T> {
    T getConnection() throws FileNotFoundException;
}
