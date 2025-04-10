package com.mary.product_microservice_sharik.service;

import com.mary.product_microservice_sharik.exception.NoDataFoundException;
import com.mary.product_microservice_sharik.model.dto.ProductSearchFilterDTO;
import com.mary.product_microservice_sharik.model.dto.SetProductStatusDTO;
import com.mary.product_microservice_sharik.model.entity.Product;
import com.mary.product_microservice_sharik.model.enumClass.SortProductBy;
import com.mary.product_microservice_sharik.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productService, "PAGE_SIZE", 9);
    }

    @Test
    void findProductsByFilterOnPage() {
        ProductSearchFilterDTO dto = new ProductSearchFilterDTO();
        dto.setNameAndDescription("coffee");
        dto.setPriceFrom(BigDecimal.valueOf(10));
        dto.setPriceTo(BigDecimal.valueOf(100));
        dto.setCategories(List.of("drinks"));
        dto.setSortBy(SortProductBy.NAME);
        dto.setSortDirection(Sort.Direction.ASC);
        dto.setPage(1);

        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productRepository.searchProductsByFilter(
                any(), any(), any(), any(), any())
        ).thenReturn(page);

        List<Product> result = productService.findProductsByFilterOnPage(dto);

        assertEquals(1, result.size());
    }

    @Test
    void setProductStatus_idNotExists_throwsException() {
        SetProductStatusDTO dto = new SetProductStatusDTO();
        dto.setProductId("invalid-id");
        dto.setStatus(true);

        when(productRepository.findById("invalid-id"))
                .thenReturn(Optional.empty());

        assertThrows(NoDataFoundException.class, () ->
                productService.setProductStatus(dto));
    }

    @Test
    void setProductStatus_idExists_saves() {
        Product product = new Product();
        product.setAvailable(false);

        SetProductStatusDTO dto = new SetProductStatusDTO();
        dto.setProductId("id");
        dto.setStatus(true);

        when(productRepository.findById("id"))
                .thenReturn(Optional.of(product));

        productService.setProductStatus(dto);

        assertTrue(product.isAvailable());
        verify(productRepository).save(product);
    }

    @Test
    void save_invalidDTO_throwsException() {
        assertThrows(NullPointerException.class, () ->
                productService.create(null));
    }

    @Test
    void findById_invalidId_throwsException() {
        when(productRepository.findById("notfound"))
                .thenReturn(Optional.empty());

        assertThrows(NoDataFoundException.class, () ->
                productService.findById("notfound"));
    }

    @Test
    void findById_validId_finds() {
        Product product = new Product();
        product.setName("Test");

        when(productRepository.findById("123"))
                .thenReturn(Optional.of(product));

        Product result = productService.findById("123");

        assertEquals("Test", result.getName());
    }

    @Test
    void findProductsByIds_validIds_finds() {
        Product p1 = new Product();
        Product p2 = new Product();

        when(productRepository.findAllById(List.of("1", "2")))
                .thenReturn(List.of(p1, p2));

        List<Product> result = productService.findProductsByIds(List.of("1", "2"));

        assertEquals(2, result.size());
    }

    @Test
    void findProductsByIds_invalidId_throwsException() {
        when(productRepository.findAllById(List.of("notfound")))
                .thenReturn(List.of());

        List<Product> result = productService.findProductsByIds(List.of("notfound"));

        assertTrue(result.isEmpty());
    }
}