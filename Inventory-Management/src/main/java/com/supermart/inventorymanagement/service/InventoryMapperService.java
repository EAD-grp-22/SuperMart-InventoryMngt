package com.supermart.inventorymanagement.service;

import com.supermart.inventorymanagement.dto.InventoryResponse;
import com.supermart.inventorymanagement.dto.ProductResponse;
import com.supermart.inventorymanagement.dto.UseMultipleResponse;
import com.supermart.inventorymanagement.model.Inventory;
import com.supermart.inventorymanagement.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryMapperService {
    public InventoryResponse mapToInventoryResponse(Inventory inventory,Double price) {
        return InventoryResponse.builder()
                .skuCode(inventory.getSkuCode())
                .quantity(inventory.getQuantity())
                .isInStock(inventory.isInStock())
                .productId(inventory.getProductId())
                .price(price)
                .build();
    }

    public Inventory mapToInventory(InventoryResponse inventoryResponse) {
        Inventory inventory=new Inventory();
        inventory.setSkuCode(inventoryResponse.getSkuCode());
        inventory.setQuantity(inventoryResponse.getQuantity());
        inventory.setProductId(inventoryResponse.getProductId());
        inventory.setInStock(inventory.isInStock());
        return inventory;
    }

    public UseMultipleResponse mapToUseMultipleResponse(List<InventoryResponse> inventories,Double totalPrice) {
        UseMultipleResponse useMultipleResponse=UseMultipleResponse.builder()
                .inventoryResponseList(inventories)
                .totalPrice(totalPrice)
                .build();
        return useMultipleResponse;
    }

}
