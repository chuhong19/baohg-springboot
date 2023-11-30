package com.example.baohg.controllers;

import com.example.baohg.dto.MessageResponse;
import com.example.baohg.models.User;
import com.example.baohg.repository.UserRepository;
import com.example.baohg.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
//  GET - localhost:8080/api/friends
    @GetMapping
    public ResponseEntity<?> getAllFriends() {
        System.out.println("A");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("B");
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        System.out.println("C");
        Long userId = user.getId();
        Set<Long> friends = userService.getFriends(userId);
        System.out.println("Friends: " + friends);
        System.out.println("D");
        return ResponseEntity.ok(friends);
    }

//  POST - localhost:8080/api/friends/{id}
    @PostMapping("/{id}")
    public MessageResponse setFriend(@PathVariable long id) {
        System.out.println("Checkpoint 1");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        System.out.println("Checkpoint 2");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        System.out.println("Checkpoint 3");
        Long userId = user.getId();
        userService.setFriend(userId, id);
        System.out.println("Checkpoint 4");
        return new MessageResponse("Added friend with id = " + id);
    }

//  DELETE - localhost:8080/api/friends/{id}
    @DeleteMapping("/{id}")
    public MessageResponse removeFriend(@PathVariable long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        Long userId = user.getId();
        userService.removeFriend(userId, id);
        return new MessageResponse("Removed friend with id = " + id);
    }

}
