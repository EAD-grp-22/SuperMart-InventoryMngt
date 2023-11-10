package com.supermart.inventorymanagement.repository;

import com.supermart.inventorymanagement.model.Inventory;
import com.supermart.inventorymanagement.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends MongoRepository<Inventory,String> {
    @Query("{skuCode: ?0}")
    Optional<Inventory> findInventoryBySkuCode(@Param("skuCode") String skuCode);

    @Query(value = "{quantity: {$gte: ?0, $lte: ?1}}")
    List<Inventory> findInventoriesByQuantityRange(@Param("minQuantity") int minQuantity, @Param("maxQuantity") int maxQuantity);

    @Query("{'productId': ?0}")
    List<Inventory> findInventoriesByProductId(@Param("productId") String productId);

    @Query("{'productId': {$in: ?0}}")
    List<Inventory> findInventoriesByProductIdList(@Param("productIds") List<String> productIds);


    void deleteAllByProductId(String productId);
}
