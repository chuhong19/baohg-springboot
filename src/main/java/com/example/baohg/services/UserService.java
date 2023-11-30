package com.example.baohg.services;

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
        System.out.println("UserService.GetFriend");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("User: " + user);
        return user.getFriends();
    }

    public void setFriend(Long userId, Long friendId) {
        System.out.println("UserService.setFriend");
        System.out.println("Checkpoint 11");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("Checkpoint 22");
        User friend = userRepository.findById(friendId).orElseThrow(() -> new RuntimeException("User to add friend not found"));
        System.out.println("Checkpoint 33");
        System.out.println("user: " + user);
        System.out.println("friend: " + friend);
        System.out.println("friendId: " + friendId);
        user.setFriend(friendId);
        System.out.println("Checkpoint 55");
        friend.setFriend(userId);
        System.out.println("Checkpoint 66");
        userRepository.save(user);
        userRepository.save(friend);
        System.out.println("Checkpoint 44");

    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        User friend = userRepository.findById(friendId).orElseThrow(() -> new RuntimeException("User to add friend not found"));
        user.removeFriend(friendId);
        friend.removeFriend(userId);
        userRepository.save(user);
        userRepository.save(friend);
    }

}
