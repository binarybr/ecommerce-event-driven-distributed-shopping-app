package com.binarylabyrinth.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Dashboard summary aggregated from every downstream service.
 * Returned by GET /api/admin/stats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDto {

    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private double totalRevenue;
    private long totalNotificationsSent;

    /** Breakdown of orders by status (PLACED, CONFIRMED, CANCELLED, etc.) */
    private Map<String, Long> ordersByStatus;

    /** Number of products with zero stock */
    private long outOfStockCount;
}
