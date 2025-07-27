package br.com.antoniogomes94.rinhabackend.controller;

import br.com.antoniogomes94.rinhabackend.model.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final BlockingQueue<PaymentRequest> paymentQueue;

    @PostMapping
    public ResponseEntity<String> createPayment(@RequestBody PaymentRequest request) {
        // Gera um transactionId se o cliente não fornecer, importante para idempotência
        if (request.getTransactionId() == null || request.getTransactionId().isEmpty()) {
            request.setTransactionId(UUID.randomUUID().toString());
        }

         request.setTimestamp(LocalDateTime.now());

        try {
            // Tenta adicionar a requisição na fila.
            // offer() retorna false se a fila estiver cheia (se tiver limite).
            // put() bloquearia a thread até conseguir adicionar.
            boolean added = paymentQueue.offer(request);

            if (added) {
                log.info("Payment request enqueued successfully: {}", request.getTransactionId());
                // Retorna 202 Accepted, indicando que a requisição foi aceita para processamento
                return ResponseEntity.accepted().body("Payment request accepted for processing.");
            } else {
                // Se a fila estiver cheia (e tiver limite), retorna 503 Service Unavailable
                log.warn("Payment queue is full. Request rejected: {}", request.getTransactionId());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Payment queue is full.");
            }
        } catch (Exception e) {
            log.error("Error enqueuing payment request: {}", request.getTransactionId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to enqueue payment request.");
        }
    }
}