package io.reactivestax.types.exceptions;

// Custom exception for optimistic locking failure
public class OptimisticLockingException extends RuntimeException {
    public OptimisticLockingException(String message) {
        super(message);
    }
}