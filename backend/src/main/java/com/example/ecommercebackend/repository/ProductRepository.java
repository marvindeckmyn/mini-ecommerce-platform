package com.example.ecommercebackend.repository;

import com.example.ecommercebackend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository provides common methods like findAll(), findById(), save(), deleteById()
}
