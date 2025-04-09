package com.mary.product_microservice_sharik.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.mary.product_microservice_sharik.model.enumClass.SortProductBy;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ProductSearchFilterDTO {
    @Size(max = 200, message = "name length must be greater than 2 and less than 200")
    private String nameAndDescription ;

    private BigDecimal priceFrom ;

    private BigDecimal priceTo ;

    private List<String> categories ;

    @Min(value = 1, message = "page number must be grater than 0")
    private Integer page ;

    @Enumerated(EnumType.STRING)
    private SortProductBy sortBy ;

    @Enumerated(EnumType.STRING)
    private Sort.Direction sortDirection;

    @JsonCreator
    public ProductSearchFilterDTO(
            @JsonProperty("nameAndDescription") String nameAndDescription,
            @JsonProperty("priceFrom") BigDecimal priceFrom,
            @JsonProperty("priceTo") BigDecimal priceTo,
            @JsonProperty("categories") List<String> categories,
            @JsonProperty("page") Integer page,
            @JsonProperty("sortBy") SortProductBy sortBy,
            @JsonProperty("sortDirection") Sort.Direction sortDirection) {

        this.nameAndDescription = (nameAndDescription == null || nameAndDescription.isEmpty()) ? "" : nameAndDescription;
        this.priceFrom = priceFrom;
        this.priceTo = priceTo;
        this.categories = (categories == null) ? new ArrayList<>() : categories;
        this.page = (page == null || page < 1) ? 1 : page;
        this.sortBy = (sortBy == null) ? SortProductBy.NAME : sortBy;
        this.sortDirection = (sortDirection == null) ? Sort.Direction.ASC : sortDirection;
    }

    public static ProductSearchFilterDTO defaultFilter() {
        return new ProductSearchFilterDTO(
                null, null,
                null, null,
                null, null, null);
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
