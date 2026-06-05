package com.binarylabyrinth.inventoryservice.consumer;

import com.binarylabyrinth.inventoryservice.entity.Inventory;
import com.binarylabyrinth.inventoryservice.repository.InventoryRepository;
import com.binarylabyrinth.message.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * ProductCreatedConsumer — registers a new inventory row whenever a product
 * is created in the catalog service.
 *
 * This keeps the inventory database in sync with the product catalog without
 * requiring a direct service-to-service call from product-service.
 *
 * Stock initialisation:
 *   The ProductCreatedEvent carries the catalog's `stock` hint (the quantity
 *   the admin typed when creating the product). That value is used as the
 *   starting inventory quantity so the product is immediately purchasable.
 *
 *   If no stock info is available (e.g. older events) the row is created with
 *   quantity 0 — the product appears in the catalog but cannot be ordered
 *   until stock is added via POST /api/inventory.
 *
 * Idempotency:
 *   If an inventory row for this productId already exists (e.g. the admin
 *   also called POST /api/inventory directly) this consumer is a no-op.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCreatedConsumer {

    private final InventoryRepository inventoryRepository;

    @KafkaListener(topics = "product-created", groupId = "inventory-product-group")
    public void consume(ProductCreatedEvent event) {
        if (event == null || event.getProductId() == null) return;

        // Idempotency guard — don't create a second row if one already exists
        if (inventoryRepository.findByProductId(event.getProductId()).isPresent()) {
            log.debug("Inventory row already exists for product {} — skipping.", event.getProductId());
            return;
        }

        int initialQty = event.getStock() != null ? event.getStock() : 0;
        Inventory inventory = Inventory.builder()
                .productId(event.getProductId())
                .quantity(initialQty)   // seed with the catalog's stock so it's orderable immediately
                .build();

        inventoryRepository.save(inventory);
        log.info("Inventory row created for new product {} ('{}') with quantity {}",
                event.getProductId(), event.getProductName(), initialQty);
    }
}
