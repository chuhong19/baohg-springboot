package com.example.baohg.controllers;

import com.example.baohg.dto.MessageResponse;
import com.example.baohg.models.Post;
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

    @GetMapping("/viewall")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
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
}
