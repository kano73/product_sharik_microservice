package com.mary.product_microservice_sharik.repository;

import com.mary.product_microservice_sharik.exception.NoDataFoundException;
import com.mary.product_microservice_sharik.model.dto.ProductSearchFilterDTO;
import com.mary.product_microservice_sharik.model.entity.Product;
import net.bytebuddy.utility.dispatcher.JavaDispatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataMongoTest
class ProductRepositoryTest {

    @Container
    static final MongoDBContainer mongoDBContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0.5"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void name() {
        Product product = new Product();
        product.setName("Coffee");
        product.setDescription("Dark roast coffee");
        product.setPrice(BigDecimal.valueOf(25));
        product.setCategories(List.of("drinks"));
        productRepository.save(product);
    }

    @Test
    void findProductsByFilter() {
        ProductSearchFilterDTO dto = ProductSearchFilterDTO.defaultFilter();

        dto.setNameAndDescription("dark");
        dto.setPriceFrom(BigDecimal.valueOf(20));
        dto.setPriceTo(BigDecimal.valueOf(30));
        dto.setCategories(List.of("drinks"));

        List<Product> products = productRepository.searchProductsByFilter(
                dto.getNameAndDescription(),
                dto.getPriceFrom(),
                dto.getPriceTo(),
                dto.getCategories(),
                PageRequest.of(dto.getPage() - 1, 10)).getContent();

        String s = products.stream().findFirst().map(Product::getName).orElseThrow(()->
                new NoDataFoundException("no prods found"));

        Assertions.assertEquals("Coffee", s);
    }
}
