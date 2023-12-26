package com.example.baohg.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse {
    public boolean success;
    public String message;
    public Object details;

    public SuccessResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
