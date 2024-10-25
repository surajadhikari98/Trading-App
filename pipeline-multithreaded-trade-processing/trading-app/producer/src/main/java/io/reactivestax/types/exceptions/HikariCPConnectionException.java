package io.reactivestax.types.exceptions;

public class HikariCPConnectionException extends RuntimeException {
    public HikariCPConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}