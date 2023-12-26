package com.example.baohg.controllers;

import com.example.baohg.dto.*;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @GetMapping("/allTransaction")
    public SuccessResponse getAllTransaction(){
        List<Transaction> transactions = transactionService.getAllTransactions();
        List<TransactionShort> outputTransactions =
                transactions.stream()
                        .map(TransactionShort::new)
                        .collect(Collectors.toList());
        SuccessResponse response = new SuccessResponse(true, "View all transactions", outputTransactions);
        return response;
    }

    @GetMapping("/allBuyTransaction")
    public SuccessResponse getAllBuyTransaction() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        List<TransactionShort> outputTransactions =
                transactions.stream()
                        .filter(transaction -> username.equals(transaction.getBuyer().getUsername()))
                        .map(TransactionShort::new)
                        .collect(Collectors.toList());
        SuccessResponse response = new SuccessResponse(true, "View all buy transactions", outputTransactions);
        return response;
    }

    @GetMapping("/allSellTransaction")
    public SuccessResponse getAllSellTransaction() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        List<TransactionShort> outputTransactions =
                transactions.stream()
                        .filter(transaction -> username.equals(transaction.getSeller().getUsername()))
                        .map(TransactionShort::new)
                        .collect(Collectors.toList());
        SuccessResponse response = new SuccessResponse(true, "View all sell transactions", outputTransactions);
        return response;
    }

    @PostMapping("/create/{id}")
    public SuccessResponse createTransaction(@PathVariable Long id, @RequestBody TransactionRequest request){

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
            SuccessResponse successResponse = new SuccessResponse(false, "TransactionController: You can't buy your product!!!");
            return successResponse;
        }
        if (buyer.getBalance() < targetProduct.getPrice()) {
            SuccessResponse successResponse = new SuccessResponse(false, "TransactionController: You don't have enough money to perform this transaction");
            return successResponse;
        }
        Long price = targetProduct.getPrice();
        String buyerPhoneNumber = request.getPhoneNumber();
        String buyerAddress = request.getAddress();
        String buyerMessage = request.getMessage();
        Long buyerId = buyer.getId();
        userService.removeBalance(buyerId, price);
        Transaction transaction = new Transaction(targetProduct, buyer, seller, price, buyerPhoneNumber, buyerAddress, buyerMessage);
        transactionRepository.save(transaction);
        SuccessResponse successResponse = new SuccessResponse(true, "TransactionController: Transaction created with your information: " + transaction);
        return successResponse;
    }

    @GetMapping("/confirm/{id}")
    public SuccessResponse confirmTransaction(@PathVariable Long id) {

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
        transactionRepository.save(transaction);
        SuccessResponse successResponse = new SuccessResponse(true, "TransactionController: Transaction confirmed with id = " + id);
        return successResponse;
    }

    // removeTz(tzId) by seller
    @DeleteMapping("/decline/{id}")
    public MessageResponse declineTransaction(@PathVariable Long id) {

        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> {
            MessageResponse messageResponse = new MessageResponse("TransactionController: Transaction not found with id: " + id);
            return new RuntimeException(messageResponse.getMessage());
        });
        // Check only seller can decline
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String sellerName = authentication.getName();
        User seller = userRepository.findByUsername(sellerName)
                .orElseThrow(() -> new RuntimeException("TransactionController: Seller not found with username: " + sellerName));
        if (seller != transaction.getSeller()) {
            throw new ValidateException("TransactionController: Only the seller can decline this transaction");
        }
        // Check confirm = false
        if (transaction.isConfirmed()) {
            throw new LogicException("TransactionController: This transaction already confirmed");
        }
        // Transfer balance
        User buyer = transaction.getBuyer();
        Long buyerId = buyer.getId();
        Long price = transaction.getPrice();
        userService.addBalance(buyerId, price);
        transactionRepository.deleteById(id);
        MessageResponse messageResponse = new MessageResponse("TransactionController: Transaction declined with id = " + id);
        return messageResponse;
    }

}
