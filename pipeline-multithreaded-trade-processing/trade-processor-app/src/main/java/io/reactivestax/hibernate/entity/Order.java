package io.reactivestax.hibernate.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "order_table") // 'order' is a reserved keyword, so we use 'order_table'
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //sequence is not supported mysql but by oracle and postgres
    private int id;

    @Column(name = "order_string")
    private String orderString;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @ManyToOne
    @JoinColumn(name = "user_id") //owning side so we put the joinColumn here
    private User user;

    // @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    // @JoinTable(
    // name = "order_product",
    // joinColumns = @JoinColumn(name = "order_id"),
    // inverseJoinColumns = @JoinColumn(name = "product_id")
    // )
    // private List<Product> products;

    // Constructors, getters, setters
}