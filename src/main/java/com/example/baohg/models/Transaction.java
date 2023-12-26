package com.example.baohg.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne
    @JoinColumn(name="product_id")
    public Product product;
    @ManyToOne
    @JoinColumn(name="buyer_id")
    public User buyer;
    @ManyToOne
    @JoinColumn(name="seller_id")
    public User seller;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    public Date createdAt;

    public boolean confirmed;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "comfired_at")
    public Date confirmedAt;

    public String buyerPhoneNumber;
    public String buyerAddress;
    public String buyerMessage;

    public Long price;

    public Transaction(Product product, User buyer, User seller, Long price, String buyerPhoneNumber, String buyerAddress, String buyerMessage) {
        this.product = product;
        this.buyer = buyer;
        this.seller = seller;
        this.price = price;
        this.confirmed = false;
        this.buyerPhoneNumber = buyerPhoneNumber;
        this.buyerAddress = buyerAddress;
        this.buyerMessage = buyerMessage;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }



}
