package br.com.antoniogomes94.rinhabackend.config;

import br.com.antoniogomes94.rinhabackend.model.PaymentRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class QueueConfig {
    @Bean
    public BlockingQueue<PaymentRequest> paymentQueue() {
        return new LinkedBlockingQueue<>();
    }
}
