package com.mary.product_microservice_sharik.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class PriceProperties {
    @Value("${price.digits.after.coma}")
    private int afterComa;

    public static int AFTER_COMA;

    @PostConstruct
    public void init() {
        AFTER_COMA = afterComa;
    }
}
