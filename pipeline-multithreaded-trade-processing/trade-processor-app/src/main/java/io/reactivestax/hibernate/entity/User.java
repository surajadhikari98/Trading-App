package io.reactivestax.hibernate.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username")
    private String username;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    // @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade =
    // CascadeType.ALL)
    // @Fetch(FetchMode.SELECT)
    // // this will turn on BATCHING and will fetch 10 orders at a time
    // // turn this off, if you want to see n+1 problem
    // @BatchSize(size = 10)
    // private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)  //since many side is owning side as it is keeping the foreign key there will be joined
    @Fetch(FetchMode.JOIN)
    private List<Order> orders = new ArrayList<>();

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                // ", address=" + address +
                // ", orders=" + orders +
                '}';
    }
}