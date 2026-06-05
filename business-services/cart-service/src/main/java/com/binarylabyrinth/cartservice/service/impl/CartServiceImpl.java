package com.binarylabyrinth.cartservice.service.impl;

import com.binarylabyrinth.cartservice.client.ProductClient;
import com.binarylabyrinth.cartservice.dto.AddToCartDto;
import com.binarylabyrinth.cartservice.dto.CartItemResponseDto;
import com.binarylabyrinth.cartservice.dto.CartResponseDto;
import com.binarylabyrinth.cartservice.entity.Cart;
import com.binarylabyrinth.cartservice.entity.CartItem;
import com.binarylabyrinth.cartservice.exception.CartException;
import com.binarylabyrinth.cartservice.repository.CartRepository;
import com.binarylabyrinth.cartservice.repository.CartItemRepository;
import com.binarylabyrinth.cartservice.service.CartService;
import com.binarylabyrinth.message.CartClearedEvent;
import com.binarylabyrinth.message.ItemAddedToCartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CartServiceImpl - shopping cart business logic.
 *
 * IMPORTANT — identity: the {@code userId} parameter here is actually the JWT
 * SUBJECT (the user's email), supplied by the controller via
 * {@code authentication.getName()}. The Cart is keyed by it (unique per user).
 *
 * Transactions: the class is {@code @Transactional} so each operation runs in
 * one unit of work — the entity graph (Cart + CartItems) is loaded, mutated in
 * memory, and flushed on commit. Cart.items uses orphanRemoval=true, so
 * removing/clearing items from the in-memory list issues DELETEs at flush.
 *
 * Pricing is owned by product-service; we fetch it once via Feign and snapshot
 * it onto the CartItem so the cart total is stable even if the catalog price
 * later changes.
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {
    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProductClient productClient;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
                           KafkaTemplate<String, Object> kafkaTemplate, ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.productClient = productClient;
    }

    @Override
    public CartResponseDto addItem(String userId, AddToCartDto request) {
        log.info("Adding item to cart for user: {}, product: {}, quantity: {}",
                userId, request.getProductId(), request.getQuantity());

        // Lazily create the cart on first add — no separate "create cart" call.
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(new Cart(userId));

        // Adding a product already in the cart MERGES quantities rather than
        // creating a duplicate line item.
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        // Capture itemPrice in both branches so we can publish it in the Kafka
        // event below WITHOUT a second Feign call to product-service.
        double itemPrice;
        if (existingItem != null) {
            // Re-use the already-snapshotted price; just bump the quantity.
            itemPrice = existingItem.getPrice();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.setUpdatedAt(LocalDateTime.now());
            log.debug("Updated quantity for existing item");
        } else {
            // New line item: fetch the current catalog price once and snapshot it.
            itemPrice = getProductPrice(request.getProductId());
            CartItem newItem = new CartItem(userId, request.getProductId(), request.getQuantity(), itemPrice);
            cart.getItems().add(newItem);
            newItem.setCart(cart);   // set both sides of the bidirectional relationship
            log.debug("Added new item to cart");
        }

        Cart saved = cartRepository.save(cart);
        publishItemAddedEvent(saved, request.getProductId(), request.getQuantity(), itemPrice);

        return mapToResponse(saved);
    }

    @Override
    public CartResponseDto updateItem(String userId, Long itemId, Integer quantity) {
        log.info("Updating cart item: {}, quantity: {}", itemId, quantity);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException("Cart not found for user: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartException("Item not found in cart"));

        if (quantity <= 0) {
            throw new CartException("Quantity must be positive");
        }

        item.setQuantity(quantity);
        item.setUpdatedAt(LocalDateTime.now());

        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Override
    public void removeItem(String userId, Long itemId) {
        log.info("Removing item from cart: {}", itemId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException("Cart not found for user: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartException("Item not found in cart"));

        cart.getItems().remove(item);
        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDto getCart(String userId) {
        log.debug("Fetching cart for user: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElse(new Cart(userId));

        return mapToResponse(cart);
    }

    @Override
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException("Cart not found for user: " + userId));

        // Count items BEFORE clearing — needed for the event payload, and the
        // list is empty after clear(). orphanRemoval=true turns this clear()
        // into DELETEs for every CartItem row on flush.
        int itemCount = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
        cart.getItems().clear();
        Cart saved = cartRepository.save(cart);

        publishCartClearedEvent(saved, itemCount);
    }

    private CartResponseDto mapToResponse(Cart cart) {
        List<CartItemResponseDto> itemDtos = cart.getItems().stream()
                .map(item -> new CartItemResponseDto(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice() * item.getQuantity(),
                        item.getAddedAt(),
                        item.getUpdatedAt()
                ))
                .toList();

        Double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        Integer itemCount = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        CartResponseDto response = new CartResponseDto();
        response.setId(cart.getId());
        response.setUserId(cart.getUserId());
        response.setItems(itemDtos);
        response.setTotalAmount(totalAmount);
        response.setItemCount(itemCount);
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        return response;
    }

    private void publishItemAddedEvent(Cart cart, String productId, Integer quantity, double price) {
        try {
            ItemAddedToCartEvent event = ItemAddedToCartEvent.builder()
                    .cartId(cart.getId())
                    .userId(cart.getUserId())
                    .productId(productId)
                    .quantity(quantity)
                    .price(price)
                    .addedAt(LocalDateTime.now())
                    .build();
            kafkaTemplate.send("item-added-to-cart", event);
            log.debug("ItemAddedToCart event published for cart: {}", cart.getId());
        } catch (Exception ex) {
            log.error("Failed to publish ItemAddedToCart event", ex);
        }
    }

    private void publishCartClearedEvent(Cart cart, int itemCount) {
        try {
            CartClearedEvent event = CartClearedEvent.builder()
                    .cartId(cart.getId())
                    .userId(cart.getUserId())
                    .itemCount(itemCount)
                    .clearedAt(LocalDateTime.now())
                    .build();
            kafkaTemplate.send("cart-cleared", event);
            log.debug("CartCleared event published for cart: {}", cart.getId());
        } catch (Exception ex) {
            log.error("Failed to publish CartCleared event", ex);
        }
    }

    private Double getProductPrice(String productId) {
        log.debug("Fetching price for product: {}", productId);

        try {
            // Call Product Service via Feign client
            return productClient.getProductById(productId).getPrice();

        } catch (feign.FeignException.NotFound e) {
            // BUGFIX: this is a Feign client, so a 404 surfaces as
            // feign.FeignException.NotFound — NOT Spring's
            // HttpClientErrorException.NotFound (which only the RestTemplate
            // throws). The previous catch never matched, so a missing product
            // fell through to the generic handler with a misleading message.
            log.error("Product not found: {}", productId);
            throw new CartException("Product not found: " + productId);

        } catch (Exception e) {
            // Network error, service down, or other unexpected issues.
            log.error("Failed to fetch product price for: {}", productId, e);
            throw new CartException("Failed to fetch product details: " + e.getMessage());
        }
    }
}
