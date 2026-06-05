package com.binarylabyrinth.adminservice.client;

import com.binarylabyrinth.adminservice.dto.external.ProductDto;
import com.binarylabyrinth.adminservice.dto.external.ProductRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/products")
    List<ProductDto> listAllProducts();

    @PostMapping("/api/products")
    ProductDto createProduct(@RequestBody ProductRequestDto product);
}
