package com.binarylabyrinth.inventoryservice.mapper;

import com.binarylabyrinth.inventoryservice.dto.InventoryRequestDto;
import com.binarylabyrinth.inventoryservice.entity.Inventory;
import org.springframework.stereotype.Component;

/**
 * InventoryMapper - Data Transfer Object Mapper
 *
 * Responsible for converting between different data representations:
 * - InventoryRequestDto (API input) ↔ Inventory (Entity)
 *
 * Benefits of mapper pattern:
 * - Decouples DTOs from entities
 * - Central place for transformation logic
 * - Easy to modify field mappings
 * - Supports different representations for different use cases
 *
 * Note: This service doesn't have a toResponseDto method because
 * InventoryResponseDto only contains a boolean flag, not entity details.
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Component
public class InventoryMapper {

    /**
     * Convert InventoryRequestDto to Inventory Entity
     * Used when adding/updating inventory from API request
     *
     * Maps:
     * - productId → productId
     * - quantity → quantity
     */
    public Inventory toEntity(
            InventoryRequestDto requestDto){

        return Inventory.builder()
                .productId(requestDto.getProductId())
                .quantity(requestDto.getQuantity())
                .build();
    }
}