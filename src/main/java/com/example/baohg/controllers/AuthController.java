package com.example.baohg.controllers;

import com.example.baohg.config.security.jwt.JwtUtils;
import com.example.baohg.dto.*;
import com.example.baohg.models.ERole;
import com.example.baohg.models.Role;
import com.example.baohg.models.User;
import com.example.baohg.repository.RoleRepository;
import com.example.baohg.repository.UserRepository;
import com.example.baohg.services.LogoutService;
import com.example.baohg.services.UserDetailsImpl;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    LogoutService logoutService;
    @Autowired
    JwtUtils jwtUtils;

    @GetMapping("/check")
    public ResponseEntity<?> checkUser(HttpServletRequest request) {
        try {
            String headerAuth = request.getHeader("Authorization");
            String jwtToken = null;
            if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
                jwtToken = headerAuth.split(" ")[1].trim();
            }
            String username = jwtUtils.getUsernameFromJwtToken(jwtToken);
            return ResponseEntity.ok(new UserResponse(true, "Check successfully!", username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "AuthController: Error: Not authenticated!"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "AuthController: Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "AuthController: Error: Email is already in use!"));
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("AuthController: Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("AuthController: Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "seller":
                        Role sellerRole = roleRepository.findByName(ERole.ROLE_SELLER)
                                .orElseThrow(() -> new RuntimeException("AuthController: Error: Role is not found."));
                        roles.add(sellerRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("AuthController: Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse(true, "User registered successfully!"));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse(true, "Login success", new JwtResponse(jwt,
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles)));
        } catch (BadCredentialsException e) {
            ApiResponse response = new ApiResponse(false, "AuthController: Wrong username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logoutService.logout(request, response, authentication);
        System.out.println("Logout success");
        return "redirect:/login";
    }
}