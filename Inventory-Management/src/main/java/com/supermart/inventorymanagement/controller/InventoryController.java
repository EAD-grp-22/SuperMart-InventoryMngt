package com.supermart.inventorymanagement.controller;

import com.supermart.inventorymanagement.dto.CreateInventoryRequest;
import com.supermart.inventorymanagement.dto.InventoryResponse;
import com.supermart.inventorymanagement.dto.UpdateInventoryRequest;
import com.supermart.inventorymanagement.dto.UseMultipleResponse;
import com.supermart.inventorymanagement.model.Inventory;
import com.supermart.inventorymanagement.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/{sku-code}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Inventory> getInventoryBySkuCode(@PathVariable("sku-code") String skuCode){
        return inventoryService.getInventoryBySkuCode(skuCode);
    }

    @GetMapping("/quantity/{minQuantity}/{maxQuantity}")
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> getInventoriesByQuantityRange(@PathVariable int minQuantity,@PathVariable int maxQuantity){
        return inventoryService.getInventoriesByQuantityRange(minQuantity, maxQuantity);
    }

    @GetMapping("/product-id/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> getInventoriesByProduct(@PathVariable String id){
        return inventoryService.getInventoriesByProduct(id);
    }

    @GetMapping("/brand/{brand}")
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> getInventoriesByBrand(@PathVariable String brand){
        return inventoryService.getInventoriesByBrand(brand);
    }

    @GetMapping("/category/{category}")
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> getInventoriesByCategory(@PathVariable String category){
        return inventoryService.getInventoriesByCategory(category);
    }

    @GetMapping("/is-in-stock/{sku-code}")
    public boolean isInStock(@PathVariable("sku-code") String skuCode){
        return inventoryService.isInStock(skuCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String addInventory(@RequestBody CreateInventoryRequest createInventoryRequest) {
        return inventoryService.addInventory(createInventoryRequest);
    }

    @PatchMapping("/restock")
    @ResponseStatus(HttpStatus.OK)
    public Inventory restockInventory(@RequestBody UpdateInventoryRequest updateInventoryRequest) {
        return inventoryService.restockInventory(updateInventoryRequest);
    }


    @PatchMapping("/restock/multiple")
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> restockMultipleInventories(@RequestBody List<UpdateInventoryRequest> updateInventoryRequests) {
        return inventoryService.restockMultipleInventories(updateInventoryRequests);
    }

    @PatchMapping("/use")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse useInventory(@RequestBody UpdateInventoryRequest updateInventoryRequest) {
        return inventoryService.useInventory(updateInventoryRequest);
    }

    @PatchMapping("/use/multiple")
    @ResponseStatus(HttpStatus.OK)
    public UseMultipleResponse useMultipleInventories(@RequestBody List<UpdateInventoryRequest> updateInventoryRequests) {
        return inventoryService.useMultipleInventories(updateInventoryRequests);
    }

    @DeleteMapping("/{sku-code}")
    public String clearInventory(@PathVariable("sku-code") String skuCode){
       return inventoryService.clearInventory(skuCode);
    }

}
