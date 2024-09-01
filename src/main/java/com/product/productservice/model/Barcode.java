package com.product.productservice.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Barcode {

    private Long id;

    private String productCode;

    private String scaleCode;

    private String cashregCode;
}
