package com.binarylabyrinth.adminservice.controller;

import com.binarylabyrinth.adminservice.dto.AdminStatsDto;
import com.binarylabyrinth.adminservice.dto.BulkImportResponseDto;
import com.binarylabyrinth.adminservice.dto.external.*;
import com.binarylabyrinth.adminservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminController — all endpoints require ROLE_ADMIN.
 * Method-level @PreAuthorize ensures the JWT role check runs even if the
 * route somehow leaks through the gateway without authorization.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /** Dashboard summary card data. */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    /** List all registered users. */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /** List all orders. */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    /** Last N orders ordered by createdAt DESC (defaults to 10). */
    @GetMapping("/orders/recent")
    public ResponseEntity<List<OrderDto>> getRecentOrders(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminService.getRecentOrders(limit));
    }

    /** Update an order's status (e.g. PLACED → SHIPPED → DELIVERED). */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(adminService.updateOrderStatus(id, status));
    }

    /** Products with stock below the given threshold (default 10). */
    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<InventoryDto>> getLowStock(@RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(adminService.getLowStock(threshold));
    }

    /** Bulk-create products. Best-effort: partial successes are reported in the response. */
    @PostMapping("/products/bulk-import")
    public ResponseEntity<BulkImportResponseDto> bulkImport(@RequestBody List<ProductRequestDto> products) {
        return ResponseEntity.ok(adminService.bulkImportProducts(products));
    }

    /** All notification records (audit log). */
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        return ResponseEntity.ok(adminService.getNotifications());
    }

    /** Just the failed notifications (for retry/debug). */
    @GetMapping("/notifications/failed")
    public ResponseEntity<List<NotificationDto>> getFailedNotifications() {
        return ResponseEntity.ok(adminService.getFailedNotifications());
    }
}
