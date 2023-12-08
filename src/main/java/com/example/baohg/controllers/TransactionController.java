package com.example.baohg.controllers;

import com.example.baohg.dto.MessageResponse;
import com.example.baohg.dto.TransactionRequest;
import com.example.baohg.exception.LogicException;
import com.example.baohg.exception.ValidateException;
import com.example.baohg.models.Product;
import com.example.baohg.models.Transaction;
import com.example.baohg.models.User;
import com.example.baohg.repository.ProductRepository;
import com.example.baohg.repository.TransactionRepository;
import com.example.baohg.repository.UserRepository;
import com.example.baohg.services.TransactionService;
import com.example.baohg.services.UserService;
import jakarta.persistence.TemporalType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    public TransactionService transactionService;
    @Autowired
    public UserDetailsService userDetailsService;
    @Autowired
    public UserService userService;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public ProductRepository productRepository;

    @GetMapping("/test")
    public void test(){
        System.out.println("Testing...");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<User> user = userRepository.findByUsername(name);
        System.out.println("user: " + user);

    }

    // createTz(productId) by user
    @PostMapping("/create/{id}")
    public MessageResponse createTransaction(@PathVariable Long id, @RequestBody TransactionRequest request){

        Product targetProduct = productRepository.findById(id)
                .orElseThrow(() -> {
                    MessageResponse messageResponse = new MessageResponse("TransactionController: Product not found with id: " + id);
                    return new RuntimeException(messageResponse.getMessage());
                });
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String buyerName = authentication.getName();
        User buyer = userRepository.findByUsername(buyerName)
                .orElseThrow(() -> new RuntimeException("TransactionController: Buyer not found with username: " + buyerName));
        User seller = targetProduct.getSeller();
        // Check buyer != seller
        if (seller == buyer) {
            throw new ValidateException("TransactionController: Are you ok bro? You can't buy your product!!!");
        }
        if (buyer.getBalance() < targetProduct.getPrice()) {
            MessageResponse messageResponse = new MessageResponse("TransactionController: You don't have enough money to perform this transaction");
            return messageResponse;
        }
        Long price = targetProduct.getPrice();
        Long buyerPhoneNumber = request.getPhoneNumber();
        String buyerAddress = request.getAddress();
        String buyerMessage = request.getMessage();
        Long buyerId = buyer.getId();
        userService.removeBalance(buyerId, price);
        Transaction transaction = new Transaction(targetProduct, buyer, seller, price, buyerPhoneNumber, buyerAddress, buyerMessage);
        transactionRepository.save(transaction);
        MessageResponse messageResponse = new MessageResponse("TransactionController: Transaction created with your information: " + transaction);
        return messageResponse;
    }
    // confirmTz(tzId) by user
    @GetMapping("/confirm/{id}")
    public MessageResponse confirmTransaction(@PathVariable Long id) {

        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> {
            MessageResponse messageResponse = new MessageResponse("TransactionController: Transaction not found with id: " + id);
            return new RuntimeException(messageResponse.getMessage());
        });
        // Check only buyer can confirm
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String buyerName = authentication.getName();
        User buyer = userRepository.findByUsername(buyerName)
                .orElseThrow(() -> new RuntimeException("TransactionController: Buyer not found with username: " + buyerName));
        if (buyer != transaction.getBuyer()) {
            throw new ValidateException("TransactionController: Only the buyer can confirm this transaction");
        }
        // Check confirm = false
        if (transaction.isConfirmed()) {
            throw new LogicException("TransactionController: This transaction already confirmed");
        }
        // Transfer balance
        User seller = transaction.getSeller();
        Long sellerId = seller.getId();
        Long price = transaction.getPrice();
        userService.addBalance(sellerId, price);
        transaction.setConfirmed(true);
        transaction.setConfirmedAt(new Date());
        System.out.println("Transaction: " + transaction);
        transactionRepository.save(transaction);
        MessageResponse messageResponse = new MessageResponse("TransactionController: Transaction confirmed with id = " + id);
        return messageResponse;

    }
    // removeTz(tzId) by seller


}
