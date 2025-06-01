package com.example.ecommercebackend.service;

import com.example.ecommercebackend.model.Cart;
import com.example.ecommercebackend.model.CartItem;
import com.example.ecommercebackend.model.Product;
import com.example.ecommercebackend.model.User;
import com.example.ecommercebackend.repository.CartItemRepository;
import com.example.ecommercebackend.repository.CartRepository;
import com.example.ecommercebackend.repository.ProductRepository;
import com.example.ecommercebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
            ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the cart for a given user. If the user does not have a cart,
     * a new one is created and associated with the user.
     * 
     * @param userId The ID of the user.
     * @return The user's cart.
     * @throws RunTimeException if the user is not found.
     */
    @Transactional
    public Cart getCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Adds a product to the user's shopping cart.
     * If the product is already in the cart, its quantity is updated.
     * Otherwise, a new cart item is created.
     * 
     * @param userId    The ID of the user.
     * @param productId The ID of the product to add.
     * @param quantity  The quantity of the product to add.
     * @return The created or updated CartItem.
     * @throws RunTimeException if the user or product is not found, or quantity is invalid.
     */
    @Transactional
    public CartItem addProductToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        Cart cart = getCartForUser(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Optional<CartItem> existingCartItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        CartItem cartItem;
        if (existingCartItemOpt.isPresent()) {
            cartItem = existingCartItemOpt.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
        } else {
            cartItem = new CartItem(cart, product, quantity);
            cart.getItems().add(cartItem);
            cartItem = cartItemRepository.save(cartItem);
        }
        return cartItem;
    }

    @Transactional
    public CartItem updateCartItemQuantity(Long userId, Long cartItemId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        Cart cart = getCartForUser(userId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found with id: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("CartItem does not belong to the current user's cart.");
        }

        if (newQuantity == 0) {
            cartItemRepository.delete(cartItem);
            cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
            return null;
        } else {
            cartItem.setQuantity(newQuantity);
            return cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public void removeProductFromCart(Long userId, Long cartItemId) {
        Cart cart = getCartForUser(userId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found with id: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("CartItem does not belong to the current user's cart.");
        }

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        if (removed) {
            cartRepository.save(cart);
        } else {
            throw new RuntimeException("CartItem not found in cart's item list, though it exists and belongs to cart.");
        }
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getCartForUser(userId);
        if (cart != null && !cart.getItems().isEmpty()) {
            cartItemRepository.deleteAllInBatch(cart.getItems());
            cart.getItems().clear();
        }
    }

    public BigDecimal calculateCartTotal(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " +cartId));

        return cart.getItems().stream()
            .map(item -> item.getPriceWhenAdded().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
