package io.reactivestax.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "journal_entries")
public class JournalEntries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "trade_id")
    private String tradeId;

    @Column(name = "trade_date")
    private String tradeDate;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name="cusip")
    private String cusip;

    @Column(name = "direction")
    private String direction;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name ="price")
    private Double price;

    @Column(name = "posted_date")
    @CreationTimestamp
    private Date postedDate;
}
