package io.reactivestax.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Trade {
    private String tradeIdentifier;
    private String tradeDateTime;
    private String accountNumber;
    private String cusip;
    private String direction;
    private Integer quantity;
    private Double price;
    private Integer position;

}
