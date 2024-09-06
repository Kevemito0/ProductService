package com.product.productservice;


import com.product.productservice.model.Barcode;
import com.product.productservice.model.Category;
import com.product.productservice.model.Product;
import com.product.productservice.model.Units;
import com.product.productservice.repo.ProductRepo;
import com.product.productservice.service.ProductService;
import jakarta.inject.Inject;
import jakarta.persistence.EnumType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class ProductServiceApplicationTests {
    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepo productRepo;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setUnit(Units.Kilogram);
        sampleProduct.setCode("21232");
        sampleProduct.setName("Sample Product");

    }

    @Test
    void getAllProducts() {
        // Arrange: Set up the mock behavior
        when(productRepo.findAll()).thenReturn(Arrays.asList(sampleProduct));

        // Act: Call the method to be tested
        List<Product> products = productRepo.findAll();

        // Assert: Verify the result
        assertEquals(1, products.size());
        assertEquals("Sample Product", products.get(0).getName());

        // Verify the interaction with the mock
        verify(productRepo, times(1)).findAll();
    }


    @Test
    void getProductById() {
        when(productRepo.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Product product = productRepo.findById(1L).get();
        assertEquals("Sample Product", product.getName());
    }


    @Test
    void testSaveProduct() throws Exception {

        when(productRepo.save(any(Product.class))).thenReturn(sampleProduct);

        Product product = productRepo.save(sampleProduct);
        assertEquals(product, sampleProduct);
        verify(productRepo, times(1)).save(sampleProduct);
    }
}