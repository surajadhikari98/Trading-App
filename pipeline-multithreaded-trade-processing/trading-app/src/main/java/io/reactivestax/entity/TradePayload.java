package io.reactivestax.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "trade_payloads")
@Data
public class TradePayload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "trade_id")
    private String tradeId;

    @Column(name = "validity_status")
    private String validityStatus;

    @Column(name = "status_reason")
    private String statusReason;

    @Column(name = "lookup_status")
    private String lookupStatus;

    @Column(name = "je_status")
    private String jeStatus;

    @Column(name = "payload")
    private String payload;

    @Column(name = "created_date_time")
    @CreationTimestamp
    private Date createdDateTime;
}
