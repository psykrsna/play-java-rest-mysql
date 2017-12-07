package v1.customer;

import com.palominolabs.http.url.UrlBuilder;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import javax.inject.Inject;
import java.nio.charset.CharacterCodingException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

/**
 * Handles presentation of Customer resources, which map to JSON.
 */
public class CustomerResourceHandler {

    private final CustomerRepository repository;
    private final HttpExecutionContext ec;

    @Inject
    public CustomerResourceHandler(CustomerRepository repository, HttpExecutionContext ec) {
        this.repository = repository;
        this.ec = ec;
    }

    public CompletionStage<Stream<CustomerResource>> find() {
        return repository.list().thenApplyAsync(customerDataStream -> {
            return customerDataStream.map(data -> new CustomerResource(data, link(data)));
        }, ec.current());
    }

    public CompletionStage<CustomerResource> create(CustomerResource resource) {
        final CustomerData data = new CustomerData(resource.getName(), resource.getLocation());
        return repository.create(data).thenApplyAsync(savedData -> {
            return new CustomerResource(savedData, link(savedData));
        }, ec.current());
    }

    public CompletionStage<Optional<CustomerResource>> lookup(String id) {
        return repository.get(Long.parseLong(id)).thenApplyAsync(optionalData -> {
            return optionalData.map(data -> new CustomerResource(data, link(data)));
        }, ec.current());
    }

    public CompletionStage<Optional<CustomerResource>> update(String id, CustomerResource resource) {
        final CustomerData data = new CustomerData(resource.getName(), resource.getLocation());
        return repository.update(Long.parseLong(id), data).thenApplyAsync(optionalData -> {
            return optionalData.map(op -> new CustomerResource(op, link(op)));
        }, ec.current());
    }

    private String link(CustomerData data) {
        // Make a point of using request context here, even if it's a bit strange
        final Http.Request request = Http.Context.current().request();
        final String[] hostPort = request.host().split(":");
        String host = hostPort[0];
        int port = (hostPort.length == 2) ? Integer.parseInt(hostPort[1]) : -1;
        final String scheme = request.secure() ? "https" : "http";
        try {
            return UrlBuilder.forHost(scheme, host, port)
                    .pathSegments("v1", "customers", data.id.toString())
                    .toUrlString();
        } catch (CharacterCodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
