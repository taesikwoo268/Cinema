package cinema.infrastructure.persistence.inmemory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import cinema.application.common.exceptions.ConflictException;
import cinema.domain.customer.Customer;

class InMemoryCustomerRepositoryTest {

    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();

    private Customer customer(String id, String userId, String displayName) {
        return new Customer(id, userId, displayName);
    }

    private static int countStored(InMemoryCustomerRepository target, String... ids) {
        int stored = 0;
        for (String id : ids) {
            if (target.findById(id).isPresent()) {
                stored++;
            }
        }
        return stored;
    }

    @Test
    void savesAndFindsCustomerById() {
        Customer customer = customer("customer-1", "user-1", "Bao Long");
        repository.save(customer);

        Customer found = repository.findById("customer-1").orElseThrow();
        assertSame(customer, found);
        assertEquals("customer-1", found.getId());
        assertEquals("user-1", found.getUserId());
        assertEquals("Bao Long", found.getDisplayName());
    }

    @Test
    void findsCustomerByUserId() {
        Customer first = customer("customer-1", "user-1", "Bao Long");
        Customer second = customer("customer-2", "user-2", "Duy Anh");
        repository.save(first);
        repository.save(second);

        assertSame(first, repository.findByUserId("user-1").orElseThrow());
        assertSame(second, repository.findByUserId("user-2").orElseThrow());
    }

    @Test
    void missingIdReturnsEmpty() {
        assertTrue(repository.findById("missing").isEmpty());
    }

    @Test
    void missingUserIdReturnsEmpty() {
        repository.save(customer("customer-1", "user-1", "Bao Long"));

        assertTrue(repository.findByUserId("missing").isEmpty());
    }

    @Test
    void duplicateIdRejectedWithoutMutatingStoredState() {
        Customer original = customer("customer-1", "user-1", "Bao Long");
        repository.save(original);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> repository.save(customer("customer-1", "user-2", "Duy Anh")));

        assertEquals("Customer ID already exists: customer-1", exception.getMessage());
        assertSame(original, repository.findById("customer-1").orElseThrow());
        assertTrue(repository.findByUserId("user-2").isEmpty());
    }

    @Test
    void duplicateUserIdRejectedWithoutMutatingStoredState() {
        Customer original = customer("customer-1", "user-1", "Bao Long");
        repository.save(original);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> repository.save(customer("customer-2", "user-1", "Duy Anh")));

        assertEquals("Customer user ID already exists: user-1", exception.getMessage());
        assertSame(original, repository.findByUserId("user-1").orElseThrow());
        assertTrue(repository.findById("customer-2").isEmpty());
    }

    @Test
    void duplicateUserIdAcrossManyCustomersKeepsFirstCustomer() {
        Customer original = customer("customer-1", "user-1", "Bao Long");
        repository.save(original);

        for (String id : new String[] {"customer-2", "customer-3", "customer-4"}) {
            assertThrows(ConflictException.class,
                    () -> repository.save(customer(id, "user-1", "Other")));
        }

        assertSame(original, repository.findByUserId("user-1").orElseThrow());
        assertEquals(1, countStored(repository, "customer-1", "customer-2", "customer-3", "customer-4"));
    }

    @Test
    void differentCustomersMayShareDisplayName() {
        Customer first = customer("customer-1", "user-1", "Bao Long");
        Customer second = customer("customer-2", "user-2", "Bao Long");
        repository.save(first);

        assertDoesNotThrow(() -> repository.save(second));
        assertSame(first, repository.findByUserId("user-1").orElseThrow());
        assertSame(second, repository.findByUserId("user-2").orElseThrow());
    }

    @Test
    void findByUserIdMatchesExactStoredValue() {
        repository.save(customer("customer-1", "user-1", "Bao Long"));

        assertTrue(repository.findByUserId(" user-1 ").isEmpty());
        assertTrue(repository.findByUserId("USER-1").isEmpty());
    }

    @Test
    void lookupsAreDeterministicRegardlessOfInsertionOrder() {
        Customer first = customer("customer-1", "user-1", "Bao Long");
        Customer second = customer("customer-2", "user-2", "Duy Anh");

        InMemoryCustomerRepository forward = new InMemoryCustomerRepository();
        forward.save(first);
        forward.save(second);

        InMemoryCustomerRepository backward = new InMemoryCustomerRepository();
        backward.save(second);
        backward.save(first);

        for (int attempt = 0; attempt < 5; attempt++) {
            assertSame(first, forward.findByUserId("user-1").orElseThrow());
            assertSame(second, forward.findByUserId("user-2").orElseThrow());
            assertSame(first, backward.findByUserId("user-1").orElseThrow());
            assertSame(second, backward.findByUserId("user-2").orElseThrow());
        }
    }

    @Test
    void missingLookupsStayEmptyAndDoNotThrow() {
        for (int attempt = 0; attempt < 3; attempt++) {
            assertTrue(repository.findByUserId(null).isEmpty());
            assertTrue(repository.findById(null).isEmpty());
            assertTrue(repository.findById("missing").isEmpty());
        }
    }

    @Test
    void exposesOnlyCustomerRepositoryContract() throws NoSuchFieldException {
        Set<String> declaredPublicMethods = Arrays.stream(InMemoryCustomerRepository.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());

        assertEquals(Set.of("findById", "findByUserId", "save"), declaredPublicMethods);
        assertFalse(Modifier.isPublic(
                InMemoryCustomerRepository.class.getDeclaredField("customers").getModifiers()),
                "Storage map must not be exposed");
    }

    @Test
    void concurrentDuplicateUserIdAllowsOneInsert() throws Exception {
        InMemoryCustomerRepository concurrentRepository = new InMemoryCustomerRepository();
        String[] ids = {"customer-0", "customer-1"};
        int threads = ids.length;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        for (String id : ids) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    concurrentRepository.save(customer(id, "user-1", "Bao Long"));
                    success.incrementAndGet();
                } catch (ConflictException e) {
                    conflict.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();
        assertTrue(doneGate.await(5, TimeUnit.SECONDS),
                "Hai tác vụ phải hoàn tất trong 5 giây");
        executor.shutdownNow();

        assertEquals(1, success.get(), "Chỉ đúng một insert thành công");
        assertEquals(1, conflict.get(), "Chỉ đúng một ConflictException");
        assertEquals(1, countStored(concurrentRepository, ids), "Chỉ một bản ghi được lưu");
        assertTrue(concurrentRepository.findByUserId("user-1").isPresent());
    }
}
