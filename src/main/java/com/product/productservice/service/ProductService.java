package com.product.productservice.service;

import com.product.productservice.model.Barcode;
import com.product.productservice.model.Category;
import com.product.productservice.model.Product;
import com.product.productservice.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@RequestMapping("/api/Products") //defining Base uri + makes simple to write wanted uri
@RequiredArgsConstructor //Lombok annotation that generates constructor for all final fields
@Service //stereotype for service
public class ProductService {
    private final RestClient restClient; //constructor injection
    String baseUrl = "http://localhost:8080/api/Barcodes";
    String CategoryBaseUrl = "http://localhost:8082/api/category/";

    private final ProductRepo productRepo; //constructor injection

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
    private boolean isCodeExists(String code){
        return productRepo.findAll().stream().anyMatch(product -> product.getCode().equals(code));
    }
    private boolean isNameExists(String name){
        return productRepo.findAll().stream().anyMatch(product -> product.getName().equals(name));
    }
    public Barcode getExternalBarcode(Long id){
        ResponseEntity<Barcode> result = restClient.get().uri(baseUrl + "/{id}",id).retrieve().toEntity(Barcode.class);
        return result.getBody();
    }
    public Category getExternalCategory(Long id){
        return restClient.get().uri(CategoryBaseUrl + "/{id}",id).retrieve().body(Category.class);
    }
    public Product getProductById(Long id) throws Exception{
        Optional<Product> productOptional = productRepo.findById(id);

        if(productOptional.isPresent()){
            return productOptional.get();
        }
        else{
            throw new Exception("Product not found");
        }

    }

    public void deleteProductById(Long id){
        try {
            restClient.delete().uri(baseUrl + "/deletebarcode/{barcode}",getProductById(id).getBarcode()).retrieve().toEntity(Product.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        productRepo.deleteById(id);

    }

    public Product updateProduct(Long id,Product product) throws Exception {
        Optional<Product> productoptional = productRepo.findById(id);
        Product savedProduct;
        Long otherProdCode = productoptional.get().getCategoryCode();
        Long prodCode = product.getCategoryCode();
        System.out.println(prodCode);
        if(productoptional.isPresent() && isCodeExists(product.getCode())){
            savedProduct = productoptional.get();
            savedProduct.setName(product.getName());
            savedProduct.setBrand(product.getBrand());
            savedProduct.setUnit(product.getUnit());
            savedProduct.setCategoryCode(product.getCategoryCode());
            if(prodCode != otherProdCode){
                savedProduct.setCode(generateProductCode(savedProduct));
                restClient.delete().uri(baseUrl + "/deletebarcode/{barcode}",getProductById(id).getBarcode()).retrieve().toEntity(Product.class);
                savedProduct.setBarcode(restClient.post().uri(baseUrl + "/create/{productCode}", savedProduct.getCode()).
                        contentType(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(Barcode.class)
                        .getProductCode());
            }
            changeProductBarcode(id, 0);
        }
        else{
            throw new RuntimeException("Product not found");
        }
        return productRepo.save(savedProduct);
    }

    public Product createProductAndBarcode (Product product) throws Exception {
        if(product == null)
            throw new IllegalAccessException("Product is null");
        if(product.getBrand().isEmpty() || product.getBrand().isBlank())
            throw new IllegalAccessException("Brand is empty");

        boolean isValidCategory = restClient.get().uri(CategoryBaseUrl + "{id}/validate", product.getCategoryCode()).retrieve().body(Boolean.class);
        boolean isNameExists = isNameExists(product.getName());
        if(isValidCategory && !isNameExists){
            product.setCode(generateProductCode(product));
            if(isCodeExists(product.getCode())){
                while(isCodeExists(product.getCode())){
                    product.setCode(generateProductCode(product));
                }
            }
                product.setBarcode(restClient.post().uri(baseUrl + "/create/{productCode}", product.getCode()).
                        contentType(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(Barcode.class)
                        .getProductCode());
                productRepo.save(product);
                changeProductBarcode(product.getId(), 0);
                return product;
        }
        //throw exception
        else throw new Exception("Invalid category or name already exists");
    }

    public Product changeProductBarcode(Long id,int place) throws Exception{
        if(productRepo.findById(id).isPresent()) {
            Product product = getProductById(id);
            String productCode = product.getBarcode();
            String productCategory = getExternalCategory(product.getCategoryCode()).getCategoryName();
            String prodUnit = String.valueOf(product.getUnit());
            Barcode barcode = restClient.get()
                    .uri(baseUrl + "/barcode/{barcode}", productCode)
                    .retrieve()
                    .body(Barcode.class);
            if (productCategory.equals("Balık")) //checks if the products category is balik or not
            {
                if (Objects.equals(prodUnit, "Kilogram")) {
                    product.setBarcode(place == 0 ? barcode.getProductCode() : barcode.getScaleCode());
                }
                if (Objects.equals(prodUnit, "Adet")) { //also for the balık
                    product.setBarcode(barcode.getCashregCode());
                }
            }
            else if (productCategory.equals("Meyve")){
                if(Objects.equals(prodUnit, "Kilogram")){
                    product.setBarcode(place == 0 ? barcode.getProductCode() : barcode.getCashregCode());
                }
                else product.setBarcode(barcode.getProductCode());

            }
            else if (productCategory.equals("Et")) {
                product.setBarcode(barcode.getScaleCode());
            } else
                product.setBarcode(barcode.getProductCode());

            return productRepo.save(product);
        }

         /*
            Barcode barcode = restClient.get()
                    .uri(baseUrl + "/barcode/{barcode}", productCode)
                    .retrieve()
                    .body(Barcode.class);

            if (productCategory.equals("Balık")) {
                if (Objects.equals(prodUnit, "Kilogram")) {
                    product.setBarcode(place == 0 ? barcode.getProductCode() : barcode.getScaleCode());
                } else if (Objects.equals(prodUnit, "Adet")) {
                    product.setBarcode(barcode.getCashregCode());
                }
            } else if (productCategory.equals("Meyve")) {
                if (Objects.equals(prodUnit, "Kilogram")) {
                    product.setBarcode(place == 0 ? barcode.getProductCode() : (place == 1 ? barcode.getCashregCode() : barcode.getProductCode()));
                } else {
                    product.setBarcode(barcode.getProductCode());
                }
            } else if (productCategory.equals("Et")) {
                product.setBarcode(barcode.getScaleCode());
            } else {
                product.setBarcode(barcode.getProductCode());
            }

            return productRepo.save(product);
            */
        else throw new Exception("Invalid category or name already exists");
    }

    public String generateProductCode(Product product){
        StringBuilder productCode = new StringBuilder();
        String categoryCode = getExternalCategory(product.getCategoryCode()).getCategoryName();
        String codeBoundries = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        productCode.append(categoryCode.substring(0, 2).toUpperCase());
        for(int i = 2; i < 5;i++){
            productCode.append(codeBoundries.charAt(random.nextInt(codeBoundries.length())));
        }
        return productCode.toString();
    }
}