package io.reactivestax.types.exception;

public class TransactionHandlingException extends RuntimeException {
    public TransactionHandlingException(String message, Throwable cause) {
        super(message, cause);
    }
}