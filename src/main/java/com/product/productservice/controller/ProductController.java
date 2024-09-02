package com.product.productservice.controller;

import com.product.productservice.model.Barcode;
import com.product.productservice.model.Category;
import com.product.productservice.model.Product;
import com.product.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/Products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    /*
    @GetMapping("/{id}")
    public Optional<Product> getProductById(@PathVariable Long id) throws Exception
    {
        Optional<Product> product = productService.getProductById(id);
        return productService.getProductById(id);
    }
    */
    @GetMapping("/{id}")
    ResponseEntity<Product> getProductById(@PathVariable("id") Long id) throws Exception {
        Product prod = productService.getProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(prod);
    }
    @GetMapping("/code/{code}")
    public ResponseEntity<Product> getProductByCode(@PathVariable String code){
        Product prod = productService.getProductBycode(code).get();
        return ResponseEntity.status(HttpStatus.OK).body(prod);
    }

    @GetMapping("/external")
    public ResponseEntity<List<Barcode>> getAllBarcodes(){
        List Barcode =  productService.getAllBarcodes();
        return ResponseEntity.status(HttpStatus.OK).body(Barcode);
    }

    @GetMapping("{prodid}/categoryName")
    public Category getExternalCategory(@PathVariable Long prodid) throws Exception {
        Long prodCategoryid = getProductById(prodid).getBody().getCategoryCode();
        return productService.getExternalCategory(prodCategoryid);
    }
    @GetMapping("/external/{id}")
    public Barcode getExternalBarcode(@PathVariable Long id)
    {
        return productService.getExternalBarcode(id);
    }
    @PostMapping
    public Product createProduct(@RequestBody Product product) throws Exception {
        return productService.createProductAndBarcode(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
    }

    @PostMapping("/externalbarcode")
    public Product createProductAndBarcode(@RequestBody Product product) throws Exception {
        return productService.createProductAndBarcode(product);
    }
    @PutMapping("{id}/update/{place}")
    public Product changeProductBarcode(@PathVariable Long id, @PathVariable int place) throws Exception {
        return productService.changeProductBarcode(id,place);
    }

    @PutMapping("/{id}/update")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) throws Exception {
        return productService.updateProduct(id, product);
    }

    /*
    @GetMapping("/code/{code}")
    public Optional<Product> getProductByCode(@PathVariable String code) {
        return productService.getProductByCode(code);
    }
    */

    /*
    @PostMapping
    public Product createProduct(@RequestBody Product product)
    {
        return productService.saveProduct(product);
    }



    @PostMapping("/externalPost")
    public Mono<Product> externalCreateProduct(@RequestBody Product product)
    {
        return productService.externalCreateProduct(product);
    }
    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable Long id)
    {
        productService.deleteProductById(id);
    }

    @GetMapping("/external/category")
    public Mono<List<Category>>getAllCategory(){
        return productService.getAllCategory();
    }
    @GetMapping("/external/kasa/{kasa}")
    public Mono<Category>getCategoryBykasa(@PathVariable String kasa){
        return productService.getCategoryBykasa(kasa);
    }
    @GetMapping("/external/terazi/{terazi}")
    public Mono<Category>getCategoryByTerazi(@PathVariable String terazi){

    /*
        List<CategoryDTO> categoryDTO = new ArrayList<>();
        categoryDTO.stream().map(CategoryDTO::getId);

        return productService.getCategoryByterazi(terazi);
    }

    @GetMapping("/external/product/{product}")
    public Mono<Category>getCategoryByproduct(@PathVariable String product){
        return productService.getCategoryByproduct(product);
    }
    */
}
