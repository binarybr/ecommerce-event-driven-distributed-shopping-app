package com.binarylabyrinth.adminservice.client;

import com.binarylabyrinth.adminservice.dto.external.InventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/api/inventory/all")
    List<InventoryDto> listAllInventory();
}
