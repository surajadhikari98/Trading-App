package io.reactivestax.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.util.Date;

@Entity
@Table(name = "positions")
public class Position {

    @Id
    @Column(name = "position_id")
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private int positionId;


    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "cusip")
    private String cusip;

    @Column(name = "position")
    private BigInteger position;

    @Column(name = "version")
    @ColumnDefault("0")
    private Integer version;

    @Column(name = "created_date_time")
    @CreationTimestamp
    private Date createdDateTime;

}
