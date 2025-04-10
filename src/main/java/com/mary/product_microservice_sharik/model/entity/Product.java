package com.mary.product_microservice_sharik.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Data
@Document(collection = "products")
public class Product{
    @Id
    private String id;

    @TextIndexed
    private String name;

    @Indexed
    private BigDecimal price;

    @TextIndexed
    private String description;

    @Indexed
    private List<String> categories;

    private Integer amountLeft;

    private String imageUrl;

    private boolean isAvailable;
}