package com.mary.product_microservice_sharik.config;

import com.mary.product_microservice_sharik.model.entity.Product;
import com.mary.product_microservice_sharik.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Component
public class DataInitializer implements ApplicationRunner{

    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        if(productRepository.count() > 0){
            return;
        }

        Random random = new Random();
        List<String> categories = Arrays.asList("house", "tea", "sport", "computer", "science");
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            Product product = new Product();
            product.setName("Product " + i);
            product.setImageUrl("https://cdn-icons-png.freepik.com/256/17205/17205369.png");
            product.setDescription("Description " + i);
            product.setPrice(random.nextInt(1000));
            product.setAmountLeft(random.nextInt(10));
            product.setAvailable(true);

            List<String> categoriesOfProduct = new ArrayList<>();
            for (String category : categories) {
                if(random.nextInt(5)>2){
                    categoriesOfProduct.add(category);
                }
            }
            product.setCategories(categoriesOfProduct);

            products.add(product);
        }

        productRepository.saveAll(products);
    }
}
