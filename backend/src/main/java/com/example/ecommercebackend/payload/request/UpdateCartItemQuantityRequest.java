package com.example.ecommercebackend.payload.request;

import jakarta.validation.constraints.Min;

public record UpdateCartItemQuantityRequest(
    @Min(value = 0, message = "Quantity cannot be negative. Use 0 to remove the item.")
    int quantity
) {}
