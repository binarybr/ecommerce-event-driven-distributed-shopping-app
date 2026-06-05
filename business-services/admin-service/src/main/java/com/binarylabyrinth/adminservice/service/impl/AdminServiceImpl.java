package com.binarylabyrinth.adminservice.service.impl;

import com.binarylabyrinth.adminservice.client.*;
import com.binarylabyrinth.adminservice.dto.AdminStatsDto;
import com.binarylabyrinth.adminservice.dto.BulkImportResponseDto;
import com.binarylabyrinth.adminservice.dto.external.*;
import com.binarylabyrinth.adminservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserClient userClient;
    private final ProductClient productClient;
    private final OrderClient orderClient;
    private final InventoryClient inventoryClient;
    private final NotificationClient notificationClient;

    @Override
    public AdminStatsDto getDashboardStats() {
        // Fan out — each downstream call is independent. A failure in one
        // service shouldn't take down the whole dashboard; degrade gracefully
        // by treating an exception as "0 / empty".
        long totalUsers     = safeListSize(this::fetchUsers);
        long totalProducts  = safeListSize(this::fetchProducts);
        List<OrderDto> orders = safeFetch(this::fetchOrders);
        List<InventoryDto> inv = safeFetch(this::fetchInventory);
        List<NotificationDto> notifs = safeFetch(this::fetchNotifications);

        double revenue = orders.stream()
                .filter(o -> o.getPrice() != null && o.getQuantity() != null)
                // CANCELLED orders shouldn't count toward gross revenue
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(o -> o.getPrice() * o.getQuantity())
                .sum();

        Map<String, Long> byStatus = orders.stream()
                .filter(o -> o.getStatus() != null)
                .collect(Collectors.groupingBy(OrderDto::getStatus, Collectors.counting()));

        long outOfStock = inv.stream()
                .filter(i -> i.getQuantity() != null && i.getQuantity() == 0)
                .count();

        long sentNotifs = notifs.stream()
                .filter(n -> "SENT".equalsIgnoreCase(n.getStatus()))
                .count();

        return AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalOrders(orders.size())
                .totalRevenue(Math.round(revenue * 100.0) / 100.0)
                .totalNotificationsSent(sentNotifs)
                .ordersByStatus(byStatus)
                .outOfStockCount(outOfStock)
                .build();
    }

    @Override
    public List<UserDto> getAllUsers() {
        return fetchUsers();
    }

    @Override
    public List<OrderDto> getAllOrders() {
        return fetchOrders();
    }

    @Override
    public List<OrderDto> getRecentOrders(int limit) {
        return fetchOrders().stream()
                .sorted(Comparator.comparing(OrderDto::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(Math.max(1, Math.min(limit, 100)))
                .toList();
    }

    @Override
    public List<InventoryDto> getLowStock(int threshold) {
        return fetchInventory().stream()
                .filter(i -> i.getQuantity() != null && i.getQuantity() < threshold)
                .sorted(Comparator.comparingInt(InventoryDto::getQuantity))
                .toList();
    }

    @Override
    public OrderDto updateOrderStatus(Long orderId, String status) {
        return orderClient.updateStatus(orderId, status);
    }

    @Override
    public BulkImportResponseDto bulkImportProducts(List<ProductRequestDto> products) {
        List<ProductDto> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (ProductRequestDto p : products) {
            try {
                ProductDto saved = productClient.createProduct(p);
                created.add(saved);
            } catch (Exception ex) {
                log.warn("Bulk-import failed for product '{}': {}", p.getName(), ex.getMessage());
                errors.add(p.getName() + ": " + ex.getMessage());
            }
        }
        return BulkImportResponseDto.builder()
                .requested(products.size())
                .succeeded(created.size())
                .failed(errors.size())
                .created(created)
                .errors(errors)
                .build();
    }

    @Override
    public List<NotificationDto> getNotifications() {
        return fetchNotifications();
    }

    @Override
    public List<NotificationDto> getFailedNotifications() {
        return fetchNotifications().stream()
                .filter(n -> "FAILED".equalsIgnoreCase(n.getStatus()))
                .toList();
    }

    // ----------------- safe fetchers (degrade on downstream failure) -----------------

    private List<UserDto> fetchUsers() {
        try { return userClient.listAllUsers(); }
        catch (Exception ex) { log.warn("userClient failure: {}", ex.getMessage()); return Collections.emptyList(); }
    }
    private List<ProductDto> fetchProducts() {
        try { return productClient.listAllProducts(); }
        catch (Exception ex) { log.warn("productClient failure: {}", ex.getMessage()); return Collections.emptyList(); }
    }
    private List<OrderDto> fetchOrders() {
        try { return orderClient.listAllOrders(); }
        catch (Exception ex) { log.warn("orderClient failure: {}", ex.getMessage()); return Collections.emptyList(); }
    }
    private List<InventoryDto> fetchInventory() {
        try { return inventoryClient.listAllInventory(); }
        catch (Exception ex) { log.warn("inventoryClient failure: {}", ex.getMessage()); return Collections.emptyList(); }
    }
    private List<NotificationDto> fetchNotifications() {
        try { return notificationClient.listAllNotifications(); }
        catch (Exception ex) { log.warn("notificationClient failure: {}", ex.getMessage()); return Collections.emptyList(); }
    }

    private static <T> long safeListSize(java.util.function.Supplier<List<T>> s) {
        return safeFetch(s).size();
    }
    private static <T> List<T> safeFetch(java.util.function.Supplier<List<T>> s) {
        try { return s.get(); } catch (Exception ex) { return Collections.emptyList(); }
    }
}
