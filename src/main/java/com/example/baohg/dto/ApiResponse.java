package com.example.baohg.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    public boolean success;
    public String message;
    public Object details;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
