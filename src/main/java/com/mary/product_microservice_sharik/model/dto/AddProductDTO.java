package com.mary.product_microservice_sharik.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class AddProductDTO {
    @NotBlank
    @Size(min = 2, max = 200)
    private String name;
    @DecimalMin("0")
    private BigDecimal price;
    @Min(0)
    private Integer amountLeft;
    @NotBlank
    @Size(min = 10, max = 2000)
    private String description;
    private String imageUrl;
    private List<String> categories;
}
