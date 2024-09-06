package com.product.productservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity //matching classes with database tables
@Getter
@Setter
@Table (name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "barcode",nullable = false)
    private String barcode;

    @Column(name = "categoryCode", nullable = false)
    private Long categoryCode;

    @Column(nullable = false,unique = true)
    private String name;

    @Column(name = "code", nullable = false,length = 5,unique = true)
    private String code;

    @Column(nullable = false)
    private String brand;

    @Enumerated(EnumType.STRING)
    private Units unit;

}
