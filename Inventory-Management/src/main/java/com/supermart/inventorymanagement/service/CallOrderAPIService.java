package com.supermart.inventorymanagement.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CallOrderAPIService {
    private final WebClient.Builder webClientBuilder;
    private final String orderMicroServiceUrl="http://ORDER-MANAGEMENT/api/order/";

    @CircuitBreaker(name = "order",fallbackMethod = "fallbackMethod")
    @Retry(name = "order")
    public Set<String> isProductUsedInOrder(List<String> skuCodes) {
        return webClientBuilder.build()
                .post()
                .uri(orderMicroServiceUrl + "/check/product")
                .bodyValue(skuCodes)
                .retrieve()
                .bodyToFlux(String.class)
                .collect(Collectors.toSet())
                .block();
    }

    @CircuitBreaker(name = "order",fallbackMethod = "fallbackMethod")
    @Retry(name = "order")
    public Set<String> isInventoryUsedInOrder(String skuCode) {
        return webClientBuilder.build()
                .get()
                .uri(orderMicroServiceUrl + "/sku-code/{sku-code}",skuCode)
                .retrieve()
                .bodyToFlux(String.class)
                .collect(Collectors.toSet())
                .block();
    }

    public Set<String> fallbackMethod(Integer userId, RuntimeException runtimeException){
        throw new RuntimeException("Error when connecting to order api");
    }
}
