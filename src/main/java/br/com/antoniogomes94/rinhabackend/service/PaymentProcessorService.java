package br.com.antoniogomes94.rinhabackend.service;

import br.com.antoniogomes94.rinhabackend.model.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentProcessorService {

    private final RestTemplate restTemplate;

    public void processAndPersistPayment(PaymentRequest request) {
        // --- Lógica de Chamada ao Payment Processor Externo (com Retries e Fallback) ---
        // Aqui é onde entra o Resilience4j ou Spring Retry!

        boolean processedSuccessfully = false;
        try {
            // Exemplo SIMPLES de chamada. Você precisará de lógica de retry robusta.
            log.info("Calling default payment processor for: {}", request.getTransactionId());
            // Chamada HTTP para o payment-processor-default
            // ResponseEntity<String> response = restTemplate.postForEntity("http://payment-processor-default:8080/process", request, String.class);
            // if (response.getStatusCode().is2xxSuccessful()) {
            //     processedSuccessfully = true;
            // }

            // Para simular:
            if (Math.random() > 0.3) { // 70% de chance de sucesso
                processedSuccessfully = true;
                log.info("Payment processed successfully by default processor: {}", request.getTransactionId());
            } else {
                log.warn("Default processor failed for: {}. Trying fallback.", request.getTransactionId());
                // Lógica de fallback
                // Simule uma chamada HTTP para o payment-processor-fallback
                // ResponseEntity<String> fallbackResponse = restTemplate.postForEntity("http://payment-processor-fallback:8081/process", request, String.class);
                // if (fallbackResponse.getStatusCode().is2xxSuccessful()) {
                //     processedSuccessfully = true;
                //     log.info("Payment processed successfully by fallback processor: {}", request.getTransactionId());
                // }
                if (Math.random() > 0.5) { // 50% de chance de sucesso no fallback
                    processedSuccessfully = true;
                    log.info("Payment processed successfully by fallback processor: {}", request.getTransactionId());
                } else {
                    log.error("Both default and fallback processors failed for: {}", request.getTransactionId());
                    // Marcar como falha ou re-enfileirar para nova tentativa depois de um tempo
                }
            }
        } catch (Exception e) {
            log.error("Error calling payment processor for {}: {}", request.getTransactionId(), e.getMessage(), e);
            // Aqui você deve decidir se tenta novamente, marca como falha temporária, etc.
        }

        // --- Lógica de Persistência no PostgreSQL ---
        if (processedSuccessfully) {
            // Aqui você persistiria os dados do pagamento no PostgreSQL usando Spring Data JPA
            // PaymentEntity paymentEntity = new PaymentEntity();
            // paymentEntity.setTransactionId(request.getTransactionId());
            // paymentEntity.setAmount(request.getAmount());
            // paymentEntity.setDescription(request.getDescription());
            // paymentEntity.setTimestamp(request.getTimestamp());
            // paymentEntity.setStatus("SUCCESS");
            // paymentRepository.save(paymentEntity);
            log.info("Payment {} persisted to DB with status SUCCESS.", request.getTransactionId());
        } else {
            // Se falhou após todas as retries, você pode persistir com status "FAILED" ou "PENDING_RETRY"
            // paymentEntity.setStatus("FAILED");
            // paymentRepository.save(paymentEntity);
            log.warn("Payment {} failed to process after all attempts. Persisting as FAILED/PENDING_RETRY.", request.getTransactionId());
        }
    }
}