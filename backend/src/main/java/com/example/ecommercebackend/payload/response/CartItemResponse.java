package com.example.ecommercebackend.payload.response;

import java.math.BigDecimal;

public record CartItemResponse(
    Long id,
    Long productId,
    String productName,
    String productDescription,
    String productImageUrl,
    int quantity,
    BigDecimal priceWhenAdded,
    BigDecimal itemTotalPrice
) {}
