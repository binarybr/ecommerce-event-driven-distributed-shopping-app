package com.binarylabyrinth.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ProductDto - minimal product view fetched from product-service.
 *
 * Used by order-service to obtain the AUTHORITATIVE unit price when placing an
 * order, so the order total is computed server-side and never trusts the
 * client-supplied price. Only the fields we actually need are declared;
 * unknown JSON fields are ignored.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("price")
    private Double price;
}
