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

@RequestMapping("/api/Products") //defining the Base uri + makes simple to write wanted uri
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
        Optional<Product> productoptional = productRepo.findById(id); // id of the product that will be updated
        Product savedProduct;
        Long otherProdCode = productoptional.get().getCategoryCode();
        Long prodCode = product.getCategoryCode();
        if(productoptional.isPresent() && isCodeExists(productoptional.get().getCode()) && !product.getName().isBlank()){
            savedProduct = productoptional.get(); // we get the product in the database for update
            savedProduct.setName(product.getName()); // setting the new name
            savedProduct.setBrand(product.getBrand()); // setting the new brand
            savedProduct.setUnit(product.getUnit()); // setting the new Unit
            savedProduct.setCategoryCode(product.getCategoryCode()); //setting the new category code
            if(prodCode != otherProdCode){ // if category code changed we need to change products code also
                savedProduct.setCode(generateProductCode(savedProduct)); // generating new product code with new category code
                restClient.delete().uri(baseUrl + "/deletebarcode/{barcode}",getProductById(id).getBarcode()).retrieve().toEntity(Product.class); // deleting old products barcode
                savedProduct.setBarcode(restClient.post().uri(baseUrl + "/create/{productCode}", savedProduct.getCode()).
                        contentType(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(Barcode.class)
                        .getProductCode()); // creating and binding new barcode for updated product with its new prodCode because of scaleCode
            }
            changeProductBarcode(id, 0); // binding default barcode for updated broduct
        }
        else{
                throw new RuntimeException("Product not found Or Category code invalid"); // if product to be updated does not exist or category code is invalid we get this error
        }
        return productRepo.save(savedProduct);
    }

    public Product createProductAndBarcode (Product product) throws Exception {
        if(product == null)
            throw new IllegalAccessException("Product is null"); // if the product that will be created is null
        if(product.getBrand().isEmpty() || product.getBrand().isBlank()) // if the brand is empty or null
            throw new IllegalAccessException("Brand is empty");

        boolean isValidCategory = restClient.get().uri(CategoryBaseUrl + "{id}/validate", product.getCategoryCode()).retrieve().body(Boolean.class); //checks if the category code is valid
        boolean isNameExists = isNameExists(product.getName()); // checks that name is already exist
        if(isValidCategory && !isNameExists){
            product.setCode(generateProductCode(product)); // creating prodCode
            if(isCodeExists(product.getCode())){
                while(isCodeExists(product.getCode())){ // loop for creating unique code because we don't want to get error when creating
                    product.setCode(generateProductCode(product)); //this works until created prodCode is not exist
                }
            }
                product.setBarcode(restClient.post().uri(baseUrl + "/create/{productCode}", product.getCode()).
                        contentType(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(Barcode.class)
                        .getProductCode()); // creating barcode from BarcodeService and takes the default product barcode
                productRepo.save(product); //saving product so changeproductbarcode function can work
                changeProductBarcode(product.getId(), 0); //placing default barcode for created product
                return product;
        }
        //throw exception
        else throw new Exception("Invalid category or name already exists");
    }

    public Product changeProductBarcode(Long id,int place) throws Exception{
        if(productRepo.findById(id).isPresent()) { //checking if the product is present in the database
            Product product = getProductById(id);
            String productCode = product.getBarcode();
            String productCategory = getExternalCategory(product.getCategoryCode()).getCategoryName();
            String prodUnit = String.valueOf(product.getUnit());
            Barcode barcode = restClient.get()
                    .uri(baseUrl + "/barcode/{barcode}", productCode)
                    .retrieve()
                    .body(Barcode.class); //getting the barcode class from barcodeService
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