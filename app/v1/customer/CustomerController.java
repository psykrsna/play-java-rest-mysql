package v1.customer;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@With(CustomerAction.class)
public class CustomerController extends Controller {

    private HttpExecutionContext ec;
    private CustomerResourceHandler handler;

    @Inject
    public CustomerController(HttpExecutionContext ec, CustomerResourceHandler handler) {
        this.ec = ec;
        this.handler = handler;
    }

    public CompletionStage<Result> list() {
        String page = request().getQueryString("page");
        return handler.find(page).thenApplyAsync(customers -> {
            final List<CustomerResource> customerList = customers.collect(Collectors.toList());
            return ok(Json.toJson(customerList));
        }, ec.current());
    }

    public CompletionStage<Result> show(String id) {
        return handler.lookup(id).thenApplyAsync(optionalResource -> {
            return optionalResource.map(resource ->
                ok(Json.toJson(resource))
            ).orElseGet(() ->
                notFound()
            );
        }, ec.current());
    }

    public CompletionStage<Result> update(String id) {
        JsonNode json = request().body().asJson();
        CustomerResource resource = Json.fromJson(json, CustomerResource.class);
        return handler.update(id, resource).thenApplyAsync(optionalResource -> {
            return optionalResource.map(r ->
                    ok(Json.toJson(r))
            ).orElseGet(() ->
                    notFound()
            );
        }, ec.current());
    }

    public CompletionStage<Result> create() {
        JsonNode json = request().body().asJson();
        final CustomerResource resource = Json.fromJson(json, CustomerResource.class);
        return handler.create(resource).thenApplyAsync(savedResource -> {
            return created(Json.toJson(savedResource));
        }, ec.current());
    }

    public CompletionStage<Result> delete(String id) {
        return handler.delete(id).thenApplyAsync(optionalResource -> {
            return optionalResource.map(r ->
            ok(Json.toJson(r))
            ).orElseGet(() ->
                    notFound()
            );
        }, ec.current());
    }
}
