package br.com.antoniogomes94.rinhabackend.service;

import br.com.antoniogomes94.rinhabackend.model.PaymentRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentWorker {

    private final BlockingQueue<PaymentRequest> paymentQueue;
    private final PaymentProcessorService paymentProcessorService;
    private ExecutorService executor;

    @PostConstruct // Este método será executado após a inicialização do Bean
    public void init() {
        // Cria um pool de threads para os workers.
        // Você pode ajustar o número de threads (2 aqui) conforme a carga e recursos.
        executor = Executors.newFixedThreadPool(2); // Exemplo: 2 threads worker
        for (int i = 0; i < 2; i++) { // Inicia 2 threads worker
            executor.submit(this::processPayments);
        }
        log.info("PaymentWorker initialized with {} threads.", 2);
    }

    private void processPayments() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                PaymentRequest request = paymentQueue.take();

                log.info("Processing payment request: {}", request.getTransactionId());

                paymentProcessorService.processAndPersistPayment(request);

                log.info("Finished processing payment request: {}", request.getTransactionId());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("PaymentWorker thread interrupted.");
            } catch (Exception e) {
                log.error("Error processing payment request: {}", e.getMessage(), e);
                // Logar o erro e decidir o que fazer:
                // - Re-enfileirar o item (com cuidado para não criar um loop infinito)
                // - Mover para uma fila de "dead letters"
                // Para a Rinha, talvez a forma mais simples seja logar e descartar se a falha for persistente
                // ou se a sua lógica de retries já tiver sido exaurida dentro de paymentProcessorService.
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
            log.info("PaymentWorker shutting down. Remaining queue size: {}", paymentQueue.size());
        }
    }
}