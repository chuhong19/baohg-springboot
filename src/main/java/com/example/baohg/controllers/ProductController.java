package com.example.baohg.controllers;

import com.example.baohg.dto.SuccessResponse;
import com.example.baohg.dto.MessageResponse;
import com.example.baohg.dto.ProductShort;
import com.example.baohg.models.Product;
import com.example.baohg.models.User;
import com.example.baohg.repository.ProductRepository;
import com.example.baohg.repository.UserRepository;
import com.example.baohg.services.ProductService;
import com.example.baohg.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/product")
@PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
public class ProductController {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    private final ProductService productService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserRepository userRepository;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/test")
    public ResponseEntity test() {
        return ResponseEntity.ok().body("Seller route");
    }

    @GetMapping("/allProduct")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    public SuccessResponse getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductShort> outputProducts =
        products.stream()
                .map(ProductShort::new)
                .collect(Collectors.toList());
        SuccessResponse response = new SuccessResponse(true, "View all products", outputProducts);
        return response;
    }

    @GetMapping("/myProduct")
    public SuccessResponse getAllMyProducts() {
        List<Product> products = productService.getAllProducts();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        List<ProductShort> outputProducts =
                products.stream()
                        .filter(product -> username.equals(product.getSeller().getUsername()))
                        .map(ProductShort::new)
                        .collect(Collectors.toList());
        SuccessResponse response = new SuccessResponse(true, "View my products", outputProducts);
        return response;
    }

    @PostMapping("/create")
    public MessageResponse createProduct(@RequestBody Product product){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("ProductController: User not found with username: " + username));

        product.setSeller(user);
        product.setCreatedAt(new Date());
        Product savedProduct = productRepository.save(product);

        MessageResponse messageResponse = new MessageResponse("ProductController: Product created with seller: " + username);
        return messageResponse;
    }

    @PutMapping("/update/{id}")
    public MessageResponse updateProduct(@PathVariable Long id, @RequestBody Product updatedProduct) {

        MessageResponse response = new MessageResponse("");
        try {
            productService.getProductById(id)
                    .map(existingProduct -> {
                        existingProduct.setName(updatedProduct.getName());
                        existingProduct.setDescription(updatedProduct.getDescription());
                        existingProduct.setUpdatedAt(updatedProduct.getUpdatedAt());
//                    existingPost.setAuthor(updatedPost.getAuthor());
                        Product savedProduct = productService.saveProduct(existingProduct);
                        response.setMessage("Product with id = " + id + "updated");
                        return response;
                    });
        } catch (Exception ex) {
            response.setMessage("ProductController: Product not found");
            return response;
        }
        response.setMessage("ProductController: Internal error");
        return response;
    }

    @DeleteMapping("/delete/{id}")
    public MessageResponse removeProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        MessageResponse messageResponse = new MessageResponse("Product deleted by id: " + id);
        return messageResponse;
    }

    @GetMapping("/details/{id}")
    public SuccessResponse getDetailsProductById(@PathVariable Long id) {
        try {
            Optional<Product> product = productService.getProductById(id);
            if (product.isPresent()) {
                ProductShort productShort = ProductShort.fromProduct(product.get());
                SuccessResponse successResponse = new SuccessResponse(true, "Product details", productShort);
                return successResponse;
            } else {
                SuccessResponse successResponse = new SuccessResponse(false, "Product not found");
                return successResponse;
            }
        } catch (Exception ex) {
            SuccessResponse successResponse = new SuccessResponse(false, "Error retrieving product details");
            return successResponse;
        }
    }

}
