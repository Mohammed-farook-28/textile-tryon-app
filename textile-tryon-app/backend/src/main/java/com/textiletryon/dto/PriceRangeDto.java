package com.textiletryon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for price range information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceRangeDto {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
