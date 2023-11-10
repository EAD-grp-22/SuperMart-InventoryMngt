package com.supermart.inventorymanagement.repository;

import com.supermart.inventorymanagement.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product,String> {
    @Query("{name: '?0'}")
    List<Product> findByName(String name);

    @Query(value = "{category:'?0'}")
    List<Product> findAllByCategory(String category);

    @Query("{brand: ?0}")
    List<Product> findAllByBrand(String brand);

    @Query("{price: { $gte: ?0, $lte: ?1 }}")
    List<Product> findByPriceRange(double minPrice, double maxPrice);

    @Query("{ 'category' : ?0, 'price' : { $gte : ?1, $lte : ?2 } }")
    List<Product> findAllByCategoryAndPriceRange(String category, double minPrice, double maxPrice);
}
