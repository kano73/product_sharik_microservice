package com.mary.product_microservice_sharik.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetProductStatusDTO {
    @NotNull
    @NotBlank
    private String productId;
    @NotNull
    private Boolean status;
}
