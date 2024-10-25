package io.reactivestax.types.enums;


public enum RabbitMQHeaders {
    X_QT("x-queue-type"),
    X_RETRIES("x-retries"),
    X_DEATH("x-death"),
    X_TTL("x-message-ttl"),
    X_DLE("x-dead-letter-exchange"),
    X_DLRK("x-dead-letter-routing-key");

    private final String headerKey;

    RabbitMQHeaders(String headerKey) {
        this.headerKey = headerKey;
    }

    public String getHeaderKey() {
        return headerKey;
    }
}
