package com.example.baohg.dto;

import lombok.*;

import java.util.Set;

@Getter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private Set<String> role;
}
