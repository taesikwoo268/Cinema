package cinema.infrastructure.config;

import cinema.application.common.interfaces.PaymentGateway;
import cinema.application.common.interfaces.PaymentRepository;
import cinema.infrastructure.payment.FakePaymentGateway;
import cinema.infrastructure.persistence.inmemory.InMemoryPaymentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("venue-dev")
public class PaymentConfiguration {

    @Bean
    public PaymentRepository paymentRepository() {
        return new InMemoryPaymentRepository();
    }

    @Bean
    public PaymentGateway paymentGateway() {
        return new FakePaymentGateway();
    }
}
