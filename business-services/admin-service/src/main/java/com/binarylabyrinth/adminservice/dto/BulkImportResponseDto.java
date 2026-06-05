package com.binarylabyrinth.adminservice.dto;

import com.binarylabyrinth.adminservice.dto.external.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportResponseDto {
    private int requested;
    private int succeeded;
    private int failed;
    private List<ProductDto> created;
    private List<String> errors;
}
