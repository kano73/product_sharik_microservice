package com.mary.product_microservice_sharik.config;

import com.mary.product_microservice_sharik.model.entity.Product;
import com.mary.product_microservice_sharik.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataInitializer implements ApplicationRunner{

    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        if(productRepository.count() > 0){
            return;
        }

        List<String> categories = Arrays.asList("house", "tea", "sport", "computer", "science", "book");
        List<Product> products = new ArrayList<>(30);

        for (int i = 0; i < 30; i++) {
            Product product = new Product();
            product.setName("Product " + i);
            product.setImageUrl("https://cdn-icons-png.freepik.com/256/17205/17205369.png");
            product.setDescription("Description " + i);
            product.setPrice(BigDecimal.valueOf((double) ThreadLocalRandom.current().nextInt(10000) / 100.0));
            product.setAmountLeft(ThreadLocalRandom.current().nextInt(15));
            product.setAvailable(true);

            List<String> categoriesOfProduct = new ArrayList<>();
            for (String category : categories) {
                if(ThreadLocalRandom.current().nextInt(5)>2){
                    categoriesOfProduct.add(category);
                }
            }
            product.setCategories(categoriesOfProduct);

            products.add(product);
        }

        productRepository.saveAll(products);
    }
}
