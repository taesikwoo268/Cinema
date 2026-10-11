package cinema.infrastructure.config;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.interfaces.*;
import cinema.infrastructure.payment.FakePaymentGateway;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Profile("mysql")
@EntityScan("cinema.infrastructure.persistence.entities")
@EnableJpaRepositories("cinema.infrastructure.persistence.repositories")
public class MysqlPersistenceConfiguration {
    @Bean public Clock clock() { return new SystemClock(); }
    @Bean public PaymentGateway paymentGateway() { return new FakePaymentGateway(); }

    @Bean public static BeanNameAutoProxyCreator commandTransactionProxy() {
        var proxy = new BeanNameAutoProxyCreator();
        proxy.setBeanNames("*CommandHandler");
        proxy.setProxyTargetClass(true);
        proxy.setInterceptorNames("mysqlCommandTransaction");
        return proxy;
    }

    @Bean public MethodInterceptor mysqlCommandTransaction(PlatformTransactionManager manager) {
        var transaction = new TransactionTemplate(manager);
        transaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return invocation -> {
            if (!invocation.getMethod().getName().equals("handle")) return invocation.proceed();
            try {
                return transaction.execute(status -> {
                    try { return invocation.proceed(); }
                    catch (RuntimeException | Error ex) { throw ex; }
                    catch (Throwable ex) { throw new IllegalStateException(ex); }
                });
            } catch (DataIntegrityViolationException | PessimisticLockingFailureException ex) {
                throw new ConflictException("Database constraint or concurrent update conflict");
            }
        };
    }
}
