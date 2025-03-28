package com.mary.product_microservice_sharik.controller;

import com.mary.product_microservice_sharik.model.dto.AddProductDTO;
import com.mary.product_microservice_sharik.model.dto.ProductSearchFilterDTO;
import com.mary.product_microservice_sharik.model.entity.Product;
import com.mary.product_microservice_sharik.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProductController {
    private final ProductService productService;

    @PostMapping("/products")
    public List<Product> getProducts(@RequestBody(required = false) @Valid ProductSearchFilterDTO dto) {
        return productService.getProductsByFilterOnPage(dto);
    }

    @GetMapping("/product")
    public Product getProduct(@RequestParam String id) {
        return productService.findById(id);
    }

    @PostMapping("/create")
    public Product createProduct(@RequestBody AddProductDTO product) {
        return productService.create(product);
    }
}