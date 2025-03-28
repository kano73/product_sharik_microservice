package com.mary.product_microservice_sharik.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.mary.product_microservice_sharik.config.PriceProperties;
import com.mary.product_microservice_sharik.model.enums.SortProductByEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductSearchFilterDTO {
    @Size(max = 200, message = "name length must be greater than 2 and less than 200")
    private String nameAndDescription ;

    private Double priceFrom ;

    private Double priceTo ;

    private List<String> categories ;

    @Min(value = 1, message = "page number must be grater than 0")
    private Integer page ;

    @Enumerated(EnumType.STRING)
    private SortProductByEnum sortBy ;

    @Enumerated(EnumType.STRING)
    private Sort.Direction sortDirection;

    @JsonCreator
    public ProductSearchFilterDTO(
            @JsonProperty("nameAndDescription") String nameAndDescription,
            @JsonProperty("priceFrom") Double priceFrom,
            @JsonProperty("priceTo") Double priceTo,
            @JsonProperty("categories") List<String> categories,
            @JsonProperty("page") Integer page,
            @JsonProperty("sortBy") SortProductByEnum sortBy,
            @JsonProperty("sortDirection") Sort.Direction sortDirection) {

        this.nameAndDescription = (nameAndDescription == null || nameAndDescription.isEmpty()) ? "" : nameAndDescription;
        this.priceFrom = priceFrom == null ? null : priceFrom * (int) Math.pow(10, PriceProperties.AFTER_COMA);
        this.priceTo = priceTo == null ? null : priceTo * (int) Math.pow(10, PriceProperties.AFTER_COMA);
        this.categories = (categories == null) ? new ArrayList<>() : categories;
        this.page = (page == null || page < 1) ? 1 : page;
        this.sortBy = (sortBy == null) ? SortProductByEnum.NAME : sortBy;
        this.sortDirection = (sortDirection == null) ? Sort.Direction.ASC : sortDirection;
    }

    @Override
    public String toString() {
        return "ProductSearchFilterDTO{" +
                "nameAndDescription='" + nameAndDescription + '\'' +
                ", priceFrom=" + priceFrom +
                ", priceTo=" + priceTo +
                ", categories=" + categories +
                ", page=" + page +
                ", sortBy=" + sortBy +
                ", sortDirection=" + sortDirection +
                '}';
    }
}
