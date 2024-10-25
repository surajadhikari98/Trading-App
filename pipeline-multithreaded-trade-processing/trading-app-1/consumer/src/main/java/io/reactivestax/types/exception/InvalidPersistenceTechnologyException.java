package io.reactivestax.types.exception;

public class InvalidPersistenceTechnologyException extends RuntimeException {
    public InvalidPersistenceTechnologyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPersistenceTechnologyException(String message) {
        super(message);
    }

}