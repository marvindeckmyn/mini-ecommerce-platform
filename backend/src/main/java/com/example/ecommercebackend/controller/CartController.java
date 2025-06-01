package com.example.ecommercebackend.controller;

import com.example.ecommercebackend.model.Cart;
import com.example.ecommercebackend.model.CartItem;
import com.example.ecommercebackend.payload.request.AddItemToCartRequest;
import com.example.ecommercebackend.payload.request.UpdateCartItemQuantityRequest;
import com.example.ecommercebackend.payload.response.CartItemResponse;
import com.example.ecommercebackend.payload.response.CartResponse;
import com.example.ecommercebackend.security.services.UserDetailsImpl;
import com.example.ecommercebackend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private CartResponse mapCartToResponse(Cart cart) {
        if (cart == null) {
            return null;
        }
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapCartItemToResponse)
                .collect(Collectors.toList());

        BigDecimal grandTotal = itemResponses.stream()
                .map(CartItemResponse::itemTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String username = (cart.getUser() != null) ? cart.getUser().getUsername() : null;

        return new CartResponse(
                cart.getId(),
                cart.getUser() != null ? cart.getUser().getId() : null,
                username,
                itemResponses,
                grandTotal);
    }

    private CartItemResponse mapCartItemToResponse(CartItem cartItem) {
        if (cartItem == null || cartItem.getProduct() == null) {
            return null;
        }
        BigDecimal itemTotalPrice = cartItem.getPriceWhenAdded() != null
                ? cartItem.getPriceWhenAdded().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                : BigDecimal.ZERO;
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getDescription(),
                cartItem.getProduct().getImageUrl(),
                cartItem.getQuantity(),
                cartItem.getPriceWhenAdded(),
                itemTotalPrice);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Cart cart = cartService.getCartForUser(userDetails.getId());
        return ResponseEntity.ok(mapCartToResponse(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(@AuthenticationPrincipal UserDetailsImpl userDetails, @Valid @RequestBody AddItemToCartRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        cartService.addProductToCart(userDetails.getId(), request.productId(), request.quantity());
        Cart updatedCart = cartService.getCartForUser(userDetails.getId());
        return ResponseEntity.ok(mapCartToResponse(updatedCart));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long cartItemId, @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        cartService.updateCartItemQuantity(userDetails.getId(), cartItemId, request.quantity());
        Cart updatedCart = cartService.getCartForUser(userDetails.getId());
        return ResponseEntity.ok(mapCartToResponse(updatedCart));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItemFromCart(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long cartItemId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        cartService.removeProductFromCart(userDetails.getId(), cartItemId);
        Cart updatedCart = cartService.getCartForUser(userDetails.getId());
        return ResponseEntity.ok(mapCartToResponse(updatedCart));
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        cartService.clearCart(userDetails.getId());
        Cart updatedCart = cartService.getCartForUser(userDetails.getId());
        return ResponseEntity.ok(mapCartToResponse(updatedCart));
    }
}
