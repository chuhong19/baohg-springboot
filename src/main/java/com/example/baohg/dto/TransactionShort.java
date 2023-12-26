package com.example.baohg.dto;

import com.example.baohg.models.Transaction;
import lombok.Data;

import java.util.Date;

public class TransactionShort {
    public Long id;
    public Long productId;
    public Long sellerId;
    public Long buyerId;
    public String buyerAddress;
    public String buyerMessage;
    public String buyerPhoneNumber;
    public Long price;
    public Date createdAt;
    public Boolean confirmed;
    public Date confirmedAt;

    public TransactionShort(Transaction transaction) {
        this.id = transaction.id;
        this.productId = transaction.getProduct().getId();
        this.sellerId = transaction.getSeller().getId();
        this.buyerId = transaction.getBuyer().getId();
        this.buyerAddress = transaction.getBuyerAddress();
        this.buyerMessage = transaction.getBuyerMessage();
        this.buyerPhoneNumber = transaction.getBuyerPhoneNumber();
        this.price = transaction.getPrice();
        this.createdAt = transaction.getCreatedAt();
        this.confirmed = transaction.isConfirmed();
        this.confirmedAt = transaction.getConfirmedAt();
    }

}
