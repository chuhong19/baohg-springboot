package com.example.baohg.controllers;

import com.example.baohg.dto.MessageResponse;
import com.example.baohg.models.Post;
import com.example.baohg.models.User;
import com.example.baohg.repository.UserRepository;
import com.example.baohg.services.PostService;
import com.example.baohg.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private final PostService postService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserRepository userRepository;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public MessageResponse createPost(@RequestBody Post post) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("PostController: User not found with username: " + username));

        post.setAuthor(user);
        post.setCreatedAt(new Date());

        Post savedPost = postService.savePost(post);
        MessageResponse response = new MessageResponse("Post created with author: " + user.getUsername());
        return response;
    }

    @PutMapping("/{id}")
    public MessageResponse updatePost(@PathVariable Long id, @RequestBody Post updatedPost) {
        MessageResponse response = new MessageResponse("");
        try {
            postService.getPostById(id)
                    .map(existingPost -> {
                        existingPost.setTitle(updatedPost.getTitle());
                        existingPost.setContent(updatedPost.getContent());
                        existingPost.setUpdatedAt(updatedPost.getUpdatedAt());
//                    existingPost.setAuthor(updatedPost.getAuthor());
                        Post savedPost = postService.savePost(existingPost);
                        response.setMessage("Post with id = " + id + "updated");
                        return response;
                    });
        } catch (Exception ex) {
            response.setMessage("PostController: Post not found");
            return response;
        }
        response.setMessage("PostController: Internal error");
        return response;
    }

    @DeleteMapping("/{id}")
    public MessageResponse deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        MessageResponse response = new MessageResponse("Post deleted with id: " + id);
        return response;
    }
}
