package v1.customer
;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

/**
 * Custom execution context wired to "customer.repository" thread pool
 */
public class CustomerExecutionContext extends CustomExecutionContext {

    @Inject
    public CustomerExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "customer.repository");
    }
}
