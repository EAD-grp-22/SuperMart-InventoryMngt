package com.supermart.inventorymanagement.service;

import com.supermart.inventorymanagement.dto.CreateInventoryRequest;
import com.supermart.inventorymanagement.dto.InventoryResponse;
import com.supermart.inventorymanagement.dto.UpdateInventoryRequest;
import com.supermart.inventorymanagement.dto.UseMultipleResponse;
import com.supermart.inventorymanagement.model.Inventory;
import com.supermart.inventorymanagement.model.Product;
import com.supermart.inventorymanagement.repository.InventoryRepository;
import com.supermart.inventorymanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    private final InventoryMapperService inventoryMapperService;

    private final CallOrderAPIService callOrderAPIService;

    public String addInventory(CreateInventoryRequest createInventoryRequest){
        Product product = productRepository.findById(createInventoryRequest.getProductId()).orElse(null);
        if (product != null){
            Inventory inventory=Inventory.builder()
                    .skuCode(createInventoryRequest.getSkuCode())
                    .quantity(createInventoryRequest.getQuantity())
                    .isInStock(true)
                    .productId(createInventoryRequest.getProductId())
                    .build();
            product.getInventory().add(inventory);
            productRepository.save(product);
            inventoryRepository.save(inventory);
            return "Inventory Added Successfully - SkuCode : " + inventory.getSkuCode();
        }
        return null;
    }

    public Optional<Inventory> getInventoryBySkuCode(String skuCode){
        return inventoryRepository.findInventoryBySkuCode(skuCode);
    }

    public List<Inventory> getInventoriesByQuantityRange(int minQuantity,int maxQuantity){
        return inventoryRepository.findInventoriesByQuantityRange(minQuantity, maxQuantity);
    }

    public List<Inventory> getInventoriesByProduct(String productId){
        return inventoryRepository.findInventoriesByProductId(productId);
    }

    public List<Inventory> getInventoriesByCategory(String category){
        List<Product> productsInCategory = productRepository.findAllByCategory(category);
        List<String> productIds = productsInCategory.stream()
                .map(Product::getId)
                .collect(Collectors.toList());
        return inventoryRepository.findInventoriesByProductIdList(productIds);
    }

    public List<Inventory> getInventoriesByBrand(String brand){
        List<Product> productsInBrand = productRepository.findAllByBrand(brand);
        List<String> productIds = productsInBrand.stream()
                .map(Product::getId)
                .collect(Collectors.toList());
        return inventoryRepository.findInventoriesByProductIdList(productIds);
    }


    public Inventory restockInventory(UpdateInventoryRequest updateInventoryRequest) {
        Optional<Inventory> optionalInventory = inventoryRepository.findInventoryBySkuCode(updateInventoryRequest.getSkuCode());
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            inventory.setQuantity(inventory.getQuantity() + updateInventoryRequest.getQuantity());
            inventory.setInStock(true);

            Optional<Product> optionalProduct = productRepository.findById(inventory.getProductId());
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                List<Inventory> productInventoryList = product.getInventory();
                for (Inventory productInventory : productInventoryList) {
                    if (productInventory.getSkuCode().equals(inventory.getSkuCode())) {
                        productInventory.setQuantity(inventory.getQuantity());
                        productInventory.setInStock(inventory.isInStock());
                        break;
                    }
                }
                inventory = inventoryRepository.save(inventory);
                productRepository.save(product);
                return inventory;
            } else {
                return null;
            }

        }
        return null;
    }



    public List<Inventory> restockMultipleInventories(List<UpdateInventoryRequest> updateInventoryRequests) {
        List<Inventory> updatedInventories = new ArrayList<>();
        for (UpdateInventoryRequest request : updateInventoryRequests) {
            Optional<Inventory> optionalInventory = inventoryRepository.findInventoryBySkuCode(request.getSkuCode());
            if (!optionalInventory.isPresent()) {
                throw new RuntimeException("Invalid SKU code: " + request.getSkuCode());
            }
        }

        for (UpdateInventoryRequest request : updateInventoryRequests) {
            Inventory inventory = restockInventory(request);
            if (inventory != null) {
                updatedInventories.add(inventory);
            } else {
                throw new RuntimeException("Restock request failed for SKU code: " + request.getSkuCode());
            }
        }
        return updatedInventories;
    }



    public InventoryResponse useInventory(UpdateInventoryRequest updateInventoryRequest) {
        Optional<Inventory> optionalInventory = inventoryRepository.findInventoryBySkuCode(updateInventoryRequest.getSkuCode());
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            int currentQuantity = inventory.getQuantity();
            if (currentQuantity >= updateInventoryRequest.getQuantity()) {
                inventory.setQuantity(currentQuantity - updateInventoryRequest.getQuantity());
                inventory.setInStock(currentQuantity - updateInventoryRequest.getQuantity() > 0);

                Double price;
                Optional<Product> optionalProduct = productRepository.findById(inventory.getProductId());
                if (optionalProduct.isPresent()) {
                    Product product = optionalProduct.get();
                    price=(product.getPrice()*updateInventoryRequest.getQuantity());

                    List<Inventory> productInventoryList = product.getInventory();
                    for (Inventory productInventory : productInventoryList) {
                        if (productInventory.getSkuCode().equals(inventory.getSkuCode())) {
                            productInventory.setQuantity(inventory.getQuantity());
                            productInventory.setInStock(inventory.isInStock());
                            break;
                        }
                    }
                    inventory = inventoryRepository.save(inventory);

                    InventoryResponse inventoryResponse = inventoryMapperService.mapToInventoryResponse(inventory,price);
                    productRepository.save(product);
                    return inventoryResponse;
                }
                return null;
            }
        }
        return null;
    }

    public UseMultipleResponse useMultipleInventories(List<UpdateInventoryRequest> updateInventoryRequests) {
        List<InventoryResponse> updatedInventories = new ArrayList<>();
        for (UpdateInventoryRequest request : updateInventoryRequests) {
            Optional<Inventory> optionalInventory = inventoryRepository.findInventoryBySkuCode(request.getSkuCode());
            if (!optionalInventory.isPresent()) {
                throw new RuntimeException("Invalid SKU code: " + request.getSkuCode());
            }
        }

        Double totalPrice=0.0;

        for (UpdateInventoryRequest request : updateInventoryRequests) {
            InventoryResponse inventoryResponse = useInventory(request);
            Double price=inventoryResponse.getPrice();
            totalPrice+=price;

            if (inventoryResponse != null) {
                updatedInventories.add(inventoryResponse);
            } else {
                throw new RuntimeException("Use request failed for SKU code: " + request.getSkuCode());
            }
        }
        UseMultipleResponse response=inventoryMapperService.mapToUseMultipleResponse(updatedInventories,totalPrice);
        return response;
    }



    public String clearInventory(String skuCode) {
        Optional<Inventory> optionalInventory = inventoryRepository.findInventoryBySkuCode(skuCode);
        if (optionalInventory.isPresent()) {
            Set<String> usedOrderNumbers=callOrderAPIService.isInventoryUsedInOrder(skuCode);
            if(usedOrderNumbers==null){
                Inventory inventory = optionalInventory.get();
                Optional<Product> optionalProduct = productRepository.findById(inventory.getProductId());
                if (optionalProduct.isPresent()) {
                    Product product = optionalProduct.get();
                    product.getInventory().removeIf(inv -> inv.getSkuCode().equals(inventory.getSkuCode()));
                    productRepository.save(product);
                }
                inventoryRepository.delete(inventory);
                return "Successfully cleared inventory";
            } else{
                return "Inventory used in orders";
            }
        } else {
            throw new RuntimeException("Inventory not found");
        }
    }

    public boolean isInStock(String skuCode) {
        Optional<Inventory> optionalInventory = inventoryRepository.findInventoryBySkuCode(skuCode);
        return optionalInventory.map(Inventory::isInStock).orElse(false);
    }

}
