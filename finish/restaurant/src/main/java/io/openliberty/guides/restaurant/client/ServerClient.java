package io.openliberty.guides.restaurant.client;

import io.openliberty.guides.models.OrderRequest;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
public class ServerClient {

    @Inject
    @ConfigProperty(name = "SERVER_SERVICE_BASE_URI", defaultValue = "http://localhost:9082") //TODO Verify port
    private String baseUri;

    private WebTarget target;

    public ServerClient() {
        this.target = null;
    }

    public Response getReady2Serve(){
        return iBuilder(webTarget())
                .get();
    }

    public Response serveOrder(String orderID){
        return iBuilder(webTarget().path("complete/" + orderID))
                .post(null);
    }

    private Invocation.Builder iBuilder(WebTarget target) {
        return target
                .request()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    //Sets target to endpoint provided by Order API
    private WebTarget webTarget() {
        if (this.target == null) {
            this.target = ClientBuilder
                    .newClient()
                    .target(baseUri)
                    .path("/server");
        }
        return this.target;
    }
}