package com.binarylabyrinth.reviewservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input for POST /api/reviews and PUT /api/reviews/{id}.
 * On PUT the productId is ignored — the existing row's productId is preserved.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {

    @NotBlank(message = "productId is required")
    private String productId;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;

    @Size(max = 2000, message = "comment must be at most 2000 characters")
    private String comment;
}
