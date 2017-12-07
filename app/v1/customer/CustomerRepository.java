package v1.customer;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public interface CustomerRepository {

    CompletionStage<Stream<CustomerData>> list();

    CompletionStage<CustomerData> create(CustomerData customerData);

    CompletionStage<Optional<CustomerData>> get(Long id);

    CompletionStage<Optional<CustomerData>> update(Long id, CustomerData customerData);

    CompletionStage<Optional<CustomerData>> remove(Long id);
}

