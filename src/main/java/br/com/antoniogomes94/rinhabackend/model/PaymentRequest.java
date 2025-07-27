package br.com.antoniogomes94.rinhabackend.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String transactionId;
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;
}
