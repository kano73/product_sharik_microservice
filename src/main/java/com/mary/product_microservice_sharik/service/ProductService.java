package com.mary.product_microservice_sharik.service;

import com.mary.product_microservice_sharik.config.PriceProperties;
import com.mary.product_microservice_sharik.exception.NoDataFoundException;
import com.mary.product_microservice_sharik.model.dto.AddProductDTO;
import com.mary.product_microservice_sharik.model.dto.ProductSearchFilterDTO;
import com.mary.product_microservice_sharik.model.dto.SetProductStatusDTO;
import com.mary.product_microservice_sharik.model.entity.Product;
import com.mary.product_microservice_sharik.repository.ProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {
    @Value("${page.size.product}")
    private Integer PAGE_SIZE;

    private final ProductRepository productRepository;

    public List<Product> findProductsByFilterOnPage(@NotNull ProductSearchFilterDTO dto) {
        Integer priceFrom = dto.getPriceFrom()==null? null : dto.getPriceFrom().intValue();
        Integer priceTo = dto.getPriceTo()==null? null : dto.getPriceTo().intValue();

        return productRepository.searchProductsByFilter(
                dto.getNameAndDescription(),
                priceFrom,
                priceTo,
                dto.getCategories(),
                PageRequest.of(
                        dto.getPage()-1,
                        PAGE_SIZE,
                        Sort.by(dto.getSortDirection() ,dto.getSortBy().toString().toLowerCase()))
        ).getContent();
    }

    public void setProductStatus(@Valid @NotNull SetProductStatusDTO dto) {
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(
                ()-> new NoDataFoundException("no product found with id: "+dto.getProductId())
        );
        product.setAvailable(dto.getStatus());
        productRepository.save(product);
    }

    public void create(@Valid @NotNull AddProductDTO dto) {
        Product product = new Product();
        product.setAvailable(false);
        product.setCategories(dto.getCategories());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setAmountLeft(dto.getAmountLeft());
        product.setImageUrl(dto.getImageUrl());

        product.setPrice((int) Math.round(dto.getPrice()) * (int) Math.pow(10, PriceProperties.AFTER_COMA));

        productRepository.save(product);
    }

    public Product findById(@NotBlank String id) {
        return productRepository.findById(id).orElseThrow(
                () -> new NoDataFoundException("no product found with id: "+id)
        );
    }
}