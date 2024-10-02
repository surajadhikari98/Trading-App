package io.reactivestax.domain;

public class Trade {
    private String tradeIdentifier;
    private String tradeDateTime;
    private String accountNumber;
    private String cusip;
    private String direction;
    private Integer quantity;
    private Double price;
    private Integer position;

    public Trade(String tradeIdentifier, String tradeDateTime, String accountNumber, String cusip, String direction, Integer quantity, Double price, Integer position) {
        this.tradeIdentifier = tradeIdentifier;
        this.tradeDateTime = tradeDateTime;
        this.accountNumber = accountNumber;
        this.cusip = cusip;
        this.direction = direction;
        this.quantity = quantity;
        this.price = price;
        this.position = position;
    }

    public String getTradeIdentifier() {
        return tradeIdentifier;
    }

    public void setTradeIdentifier(String tradeIdentifier) {
        this.tradeIdentifier = tradeIdentifier;
    }

    public String getTradeDateTime() {
        return tradeDateTime;
    }

    public void setTradeDateTime(String tradeDateTime) {
        this.tradeDateTime = tradeDateTime;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCusip() {
        return cusip;
    }

    public void setCusip(String cusip) {
        this.cusip = cusip;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
