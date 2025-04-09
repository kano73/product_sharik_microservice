package com.mary.product_microservice_sharik.repository;


import com.mary.product_microservice_sharik.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    @Query("{ $and: [ "
            + " { $or: [ "
            + "   { 'name': { $regex: ?0, $options: 'i' } }, "
            + "   { 'description': { $regex: ?0, $options: 'i' } } "
            + " ] }, "
            + " { $expr: { $or: [ { $eq: [ ?1, null ] }, { $gte: [ '$price', ?1 ] } ] } }, "
            + " { $expr: { $or: [ { $eq: [ ?2, null ] }, { $lte: [ '$price', ?2 ] } ] } }, "
            + " { $or: [ { $expr: { $eq: [ ?#{#categories == null || #categories.empty}, true ] } },"
            + " { 'categories': { $in: ?3 } } ] } "
            + " ] }")
    Page<Product> searchProductsByFilter(String nameOrDescription,
                                         BigDecimal priceFrom, BigDecimal priceTo,
                                         List<String> categories, Pageable pageable);
}
