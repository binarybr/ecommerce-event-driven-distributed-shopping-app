package com.binarylabyrinth.adminservice.client;

import com.binarylabyrinth.adminservice.dto.external.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders")
    List<OrderDto> listAllOrders();

    @PutMapping("/api/orders/{id}/status")
    OrderDto updateStatus(@PathVariable("id") Long id, @RequestParam("status") String status);
}
