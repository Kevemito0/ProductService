package com.product.productservice.service;

import com.product.productservice.model.Barcode;
import com.product.productservice.model.Category;
import com.product.productservice.model.Product;
import com.product.productservice.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/Products")
@RequiredArgsConstructor
@Service
public class ProductService {
    private final RestClient restClient;
    String baseUrl = "http://localhost:8080/api/Barcodes";
    String CategoryBaseUrl = "http://localhost:8082/api/category/";
    @Autowired
    private ProductRepo productRepo;

    private Random random = new Random();


    public List<Product> getAllProducts(){
        return productRepo.findAll();
    }
    public List<Barcode> getAllBarcodes(){
        return restClient.get().uri(baseUrl).retrieve().body(List.class);
    }
    public Optional<Product> getProductBycode(String code){
        return productRepo.findAll().stream().filter(product -> product.getCode().equals(code)).findFirst();
    }
    public Barcode getExternalBarcode(Long id){
        ResponseEntity<Barcode> result = restClient.get().uri(baseUrl + "/{id}",id).retrieve().toEntity(Barcode.class);
        return result.getBody();
    }
    public Category getExternalCategory(Long id){
        return restClient.get().uri(CategoryBaseUrl + "/{id}",id).retrieve().body(Category.class);
    }
    public Optional<Product> getProductById(Long id) throws Exception{
        Optional<Product> product = productRepo.findById(id);
        if(product.isPresent()){
            return product;
        }
        else{
            throw new Exception("Product not found");
        }
    }

    public Product saveProduct(Product product){
        return productRepo.save(product);
    }

    public void deleteProductById(Long id){
        try {
            restClient.delete().uri(baseUrl + "/deletebarcode/{barcode}",getProductById(id).get().getBarcode()).retrieve().toEntity(Product.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        productRepo.deleteById(id);

    }

    public Product updateProduct(Long id,Product product) throws Exception {
        Optional<Product> productoptional = productRepo.findById(id);
        Product savedProduct = productRepo.findById(id).get();
        if(productoptional.isPresent()){
            savedProduct.setName(product.getName());
            savedProduct.setBrand(product.getBrand());
            savedProduct.setUnit(product.getUnit());
            savedProduct.setCategoryCode(product.getCategoryCode());
            changeProductBarcode(id, 0);
        }
        else{
            throw new RuntimeException("Product not found");
        }
        return productRepo.save(savedProduct);
    }

    /*
    public Mono<Product> getProductFromOtherService(String id){
        return RestClient.get().uri("/api/service1/Products/{id}",id).retrieve().bodyToMono(Product.class);
    }
    public Mono<List<Product>> getAllProductsExternal(){
        return webClient.get().uri("/api/service1/Products").retrieve().bodyToMono(new ParameterizedTypeReference<List<Product>>() {});
    }
    public Mono<Product> externalCreateProduct(Product product){
        return webClient.post().uri("/api/service1/Products").bodyValue(product).retrieve().bodyToMono(Product.class);
    }
    public Mono<Product> getProductByCode(String code){
        return webClient.get().uri("/api/service1/Products/code/{code}",code).retrieve().bodyToMono(Product.class);
    }

    public Mono<List<Category>> getAllCategory(){
        return webClient.get().uri("/api/Categories").retrieve().bodyToMono(new ParameterizedTypeReference<List<Category>>() {});
    }

    public Mono<Category> getCategoryBykasa(String kasa){
        return webClient.get().uri("/api/Categories/kasa/{kasa}",kasa).retrieve().bodyToMono(Category.class);
    }

    public Mono<Category> getCategoryByterazi(String terazi){
        return webClient.get().uri("/api/Categories/terazi/{terazi}",terazi).retrieve().bodyToMono(Category.class);
    }

    public Mono<Category> getCategoryByproduct(String product){
        return webClient.get().uri("/api/Categories/product/{product}",product).retrieve().bodyToMono(Category.class);
    }
    */
    public Product createProductAndBarcode(Product product){
        Optional<Product> existingProduct = getProductBycode(product.getCode());
        Boolean isValidCategory = restClient.get().uri(CategoryBaseUrl + "{id}", product.getCategoryCode()).retrieve().body(Boolean.class);
        if(existingProduct.isPresent()){
            System.out.println("Products exists: " + product.getName() + ", " + product.getCode());
            return existingProduct.get();
        }
        else if(isValidCategory){
            product.setBarcode(restClient.post().uri(baseUrl + "/create").
                    contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Barcode.class)
                    .getProductCode());
            return productRepo.save(product);
        }
        //throw exception
        else return null;
    }
    public Product changeProductBarcode(Long id,int place) throws Exception{
        if(productRepo.findById(id).isPresent()) {
            Product product = getProductById(id).get();
            String productCode = product.getBarcode();
            String productCategory = getExternalCategory(product.getCategoryCode()).getCategoryName();
            System.out.println(productCategory);
            String prodUnit = String.valueOf(product.getUnit());

            if (productCategory.equals("Balık")) //checks if the products category is balik or not
            {
                if (Objects.equals(prodUnit, "Kilogram")) {
                    if (place == 0)
                        product.setBarcode(restClient.get().uri(baseUrl + "/barcode/{barcode}", productCode).retrieve().body(Barcode.class).getScaleCode());
                    else
                        product.setBarcode(restClient.get().uri(baseUrl + "/barcode/{barcode}", productCode).retrieve().body(Barcode.class).getProductCode());
                }
                if (Objects.equals(prodUnit, "Adet")) { //also for the balık
                    product.setBarcode(restClient.get().uri(baseUrl + "/barcode/{barcode}", productCode).retrieve().body(Barcode.class).getCashregCode());
                }
            } else if (productCategory.equals("Meyve")) {
                if (place == 0)
                    product.setBarcode(restClient.get().uri(baseUrl + "/barcode/{barcode}", productCode).retrieve().body(Barcode.class).getCashregCode());
                else
                    product.setBarcode(restClient.get().uri(baseUrl + "/barcode/{barcode}", productCode).retrieve().body(Barcode.class).getProductCode());
            } else if (productCategory.equals("Et")) {
                product.setBarcode(restClient.get().uri(baseUrl + "/barcode/{barcode}", productCode).retrieve().body(Barcode.class).getScaleCode());
            } else
                product.setBarcode(restClient.get().uri(baseUrl + "/barcode/{barcode}", productCode).retrieve().body(Barcode.class).getProductCode());

            return productRepo.save(product);
        }
        else
            throw new Exception("Product not found");
    }
    /*
    //generating only product barcode
    private String generateRandomBarcode(String code, int length)
    {
        int barcodelength = length;
        String barcodeLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder barcode = new StringBuilder (code);
        for(int i = 2; i < barcodelength; i++)
        {
            int index = random.nextInt(barcodeLetter.length());
            barcode.append(barcodeLetter.charAt(index));
        }
        return barcode.toString();
    }
    public Mono<Product> updateProductBarcode(Long id) {
        return Mono.defer(() -> {
            Optional<Product> productOptional = productRepo.findById(id);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                return getCategoryBykasa("1234")
                        .flatMap(category -> {
                            String newBarcode = category.getKasaCode();
                            product.setCode(newBarcode);
                            productRepo.save(product);
                            return Mono.just(product);
                        });
            } else {
                return Mono.empty();
            }
        });
    }

    public Product updateProductBarcode(Long id){
        Optional<Product> productOptional = productRepo.findById(id);
        String productBaseCode;
        if(productOptional.isPresent()){
            Product product = productOptional.get();
            productBaseCode = product.getCategory_code();
            String newBarcode = generateRandomBarcode(productBaseCode);
            product.setCode(newBarcode);
            productRepo.save(product);
            System.out.println("Updated barcode for product with ID: " + id + " to " + newBarcode);
            return product;
        }
        else {
            System.out.println("Product with ID: " + id + " not found");
            return null;
        }
    } */
}