package com.example.baohg.services;

import com.example.baohg.exception.LogicException;
import com.example.baohg.exception.NotFoundException;
import com.example.baohg.models.User;
import com.example.baohg.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Set<Long> getFriends(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("UserService: User not found"));
        return user.getFriends();
    }

    public void setFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("UserService: User not found"));
        User friend = userRepository.findById(friendId).orElseThrow(() -> new RuntimeException("UserService: User to add friend not found"));
        user.setFriend(friendId);
        friend.setFriend(userId);
        userRepository.save(user);
        userRepository.save(friend);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("UserService: User not found"));
        User friend = userRepository.findById(friendId).orElseThrow(() -> new RuntimeException("UserService: User to add friend not found"));
        user.removeFriend(friendId);
        friend.removeFriend(userId);
        userRepository.save(user);
        userRepository.save(friend);
    }

    public void addBalance(Long userId, Long amount) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("UserService: User not found with id: " + userId));
        Long currentBalance = user.getBalance();
        Long newBalance = currentBalance + amount;
        user.setBalance(newBalance);
        userRepository.save(user);
    }

    public void removeBalance(Long userId, Long amount) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("UserService: User not found with id: " + userId));
        Long currentBalance = user.getBalance();
        if (currentBalance < amount) throw new LogicException("UserService: You don't have enough money to perform this transaction");
        Long newBalance = currentBalance - amount;
        user.setBalance(newBalance);
        userRepository.save(user);
    }
}