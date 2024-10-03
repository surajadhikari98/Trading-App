package io.reactivestax.domain;

public record Trade(String tradeIdentifier, String tradeDateTime, String accountNumber, String cusip, String direction, Integer quantity, Double price, Integer position) {
}
