package v1.customer;

import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that provides a non-blocking API with a custom execution context
 * and circuit breaker.
 */
@Singleton
public class JPACustomerRepository implements CustomerRepository {

    private final JPAApi jpaApi;
    private final CustomerExecutionContext ec;
    private final CircuitBreaker circuitBreaker = new CircuitBreaker().withFailureThreshold(1).withSuccessThreshold(3);

    @Inject
    public JPACustomerRepository(JPAApi api, CustomerExecutionContext ec) {
        this.jpaApi = api;
        this.ec = ec;
    }

    @Override
    public CompletionStage<Stream<CustomerData>> list() {
        return supplyAsync(() -> wrap(em -> select(em)), ec);
    }

    @Override
    public CompletionStage<CustomerData> create(CustomerData customerData) {
        return supplyAsync(() -> wrap(em -> insert(em, customerData)), ec);
    }

    @Override
    public CompletionStage<Optional<CustomerData>> get(Long id) {
        return supplyAsync(() -> wrap(em -> Failsafe.with(circuitBreaker).get(() -> lookup(em, id))), ec);
    }

    @Override
    public CompletionStage<Optional<CustomerData>> remove(Long id) {
        return supplyAsync(() -> wrap(em -> Failsafe.with(circuitBreaker).get(() -> remove(em, id))), ec);
    }

    @Override
    public CompletionStage<Optional<CustomerData>> update(Long id, CustomerData customerData) {
        return supplyAsync(() -> wrap(em -> Failsafe.with(circuitBreaker).get(() -> modify(em, id, customerData))), ec);
    }

    private <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }

    private Optional<CustomerData> lookup(EntityManager em, Long id) throws SQLException {
        //throw new SQLException("Call this to cause the circuit breaker to trip");
        return Optional.ofNullable(em.find(CustomerData.class, id));
    }

    private Stream<CustomerData> select(EntityManager em) {
        TypedQuery<CustomerData> query = em.createQuery("SELECT p FROM CustomerData p", CustomerData.class);
        return query.getResultList().stream();
    }

    private Optional<CustomerData> remove(EntityManager em, Long id) throws SQLException {
        //throw new SQLException("Call this to cause the circuit breaker to trip");
        CustomerData customer = em.find(CustomerData.class, id);
        em.remove(customer);
        return Optional.ofNullable(em.find(CustomerData.class, id));
    }

    private Optional<CustomerData> modify(EntityManager em, Long id, CustomerData customerData) throws InterruptedException {
        final CustomerData data = em.find(CustomerData.class, id);
        if (data != null) {
            data.name = customerData.name;
            data.location = customerData.location;
        }
        Thread.sleep(10000L);
        return Optional.ofNullable(data);
    }

    private CustomerData insert(EntityManager em, CustomerData customerData) {
        return em.merge(customerData);
    }
}
