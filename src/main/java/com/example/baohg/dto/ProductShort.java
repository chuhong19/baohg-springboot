package com.example.baohg.dto;

import com.example.baohg.models.Product;
import com.example.baohg.models.User;
import jakarta.persistence.*;

import java.util.Date;

public class ProductShort {
    public Long id;
    public String name;
    public String description;
    public Long price;
    public String imageUrl;
    public Date createdAt;
    public Date updatedAt;
    public String sellerName;

    public ProductShort(Product product) {
        this.id = product.id;
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.imageUrl = product.getImageUrl();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
        this.sellerName = product.getSeller().username;
    }

    public static ProductShort fromProduct(Product product) {
        return new ProductShort(product);
    }
}
