package com.mary.product_microservice_sharik.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Document(collection = "products")
public class Product implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;

    @Id
    private String id;
    private String name;
    private Integer price;
    private Integer amountLeft;
    private String description;
    private String imageUrl;
    private List<String> categories;
    private boolean isAvailable;
}
