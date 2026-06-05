package com.binarylabyrinth.adminservice.service;

import com.binarylabyrinth.adminservice.dto.AdminStatsDto;
import com.binarylabyrinth.adminservice.dto.BulkImportResponseDto;
import com.binarylabyrinth.adminservice.dto.external.*;

import java.util.List;

public interface AdminService {

    AdminStatsDto getDashboardStats();

    List<UserDto> getAllUsers();

    List<OrderDto> getAllOrders();

    List<OrderDto> getRecentOrders(int limit);

    List<InventoryDto> getLowStock(int threshold);

    OrderDto updateOrderStatus(Long orderId, String status);

    BulkImportResponseDto bulkImportProducts(List<ProductRequestDto> products);

    List<NotificationDto> getNotifications();

    List<NotificationDto> getFailedNotifications();
}
