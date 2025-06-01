package com.example.ecommercebackend.payload.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
    Long id,
    Long userId,
    String username,
    List<CartItemResponse> items,
    BigDecimal grandTotal
) {}
