package controller;

import handler.TransporturiHandler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class TransporturiController implements TransporturiHandler {

  private Router router;

  public TransporturiController(Vertx vertx, Router router) {
    this.router = router;
    router.get("/:id/").handler(this::getTransport);
    router.post().handler(this::inregistrareTransport);
    router.get().handler(this::toateTransporturile);
  }


  @Override
  public void handle(final RoutingContext context) {
    router.handleContext(context);
  }

  private void getTransport(RoutingContext context) {

    //Option A: do you business logic here
  }

  private void inregistrareTransport(RoutingContext context) {
    //Option B: send an eventbus message, handle the message in the MainVerticle and serve the response here
  }

  private void toateTransporturile(RoutingContext context) {
//     Get the address of the request
      String address = context.request().connection().remoteAddress().toString();
      // Get the query parameter "name"
      MultiMap queryParams = context.queryParams();
      String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
      // Write a json response
      context.json(
        new JsonObject()
          .put("name", name)
          .put("address", address)
          .put("message", "Hello " + name + " connected from " + address)
      );
  }
}
