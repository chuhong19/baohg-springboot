package com.example.baohg.dto;

import lombok.*;

@Getter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    public String phoneNumber;
    public String address;
    public String message;
}
