package io.reactivestax.repository.hibernate.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.util.Date;

@Entity
@Table(name = "positions",uniqueConstraints = @UniqueConstraint(columnNames = {"account_number", "cusip"}))
@Data
public class Position {

    @Id
    @Column(name = "position_id")
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private int positionId;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "cusip")
    private String cusip;

    @Column(name = "direction")
    private String direction;

    @Column(name = "position")
    private BigInteger position;

    @Column(name = "version")
    @ColumnDefault("0")
    private Integer version;

    @Column(name = "created_date_time")
    @CreationTimestamp
    private Date createdDateTime;


    @PrePersist
    public void prePersist() {
        if (this.version == null) {
            this.version = 0;  // Set default if not already set
        }
    }
}
