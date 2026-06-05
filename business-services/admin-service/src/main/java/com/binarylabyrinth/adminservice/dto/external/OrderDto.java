package com.binarylabyrinth.adminservice.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDto {
    private Long id;
    private String orderNumber;
    private String userId;
    private String productId;
    private Integer quantity;
    private Double price;
    private String status;
    private LocalDateTime createdAt;
}
