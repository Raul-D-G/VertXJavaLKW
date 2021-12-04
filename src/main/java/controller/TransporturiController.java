package controller;

import handler.TransporturiHandler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class TransporturiController implements TransporturiHandler {

  private Router router;
  private Vertx vertx;
  public static final String LIST_ALL_TRANSPORTURI_ADDR = "VertXJavaLKW.VertXJavaLKW.list_all_transporturi";
  public static final String ADD_TRANSPORT_ADD = "VertXJavaLKW.VertXJavaLKW.add_transport";
  public static final String GET_TRANSPORT_BY_ID_ADDR = "VertXJavaLKW.VertXJavaLKW.get_transport_by_id";
  public static final String UPDATE_TRANSPORT_ADDR = "VertXJavaLKW.VertXJavaLKW.update_transport";

  public TransporturiController(Vertx vertx, Router router) {
    this.vertx = vertx;
    this.router = router;
    router.get("/:id/").handler(this::getTransport);
    router.post().handler(this::inregistrareTransport);
    router.get().handler(this::toateTransporturile);
    router.put().handler(this::updateTransport);
  }


  @Override
  public void handle(final RoutingContext context) {
    router.handleContext(context);
  }

  private void getTransport(RoutingContext context) {
    DeliveryOptions options = new DeliveryOptions();
    MultiMap queryParams = context.queryParams();
    String id = queryParams.contains("id") ? queryParams.get("id") : null;

    vertx.eventBus().request(GET_TRANSPORT_BY_ID_ADDR, id, options, reply -> {
      if (reply.succeeded()) {

        JsonObject body = (JsonObject) reply.result().body();

        context.json(body);

      } else {
        context.fail(reply.cause());
      }
    });

  }

  private void inregistrareTransport(RoutingContext context) {
    DeliveryOptions options = new DeliveryOptions();

    vertx.eventBus().request(ADD_TRANSPORT_ADD, context.getBodyAsJson(), options, reply -> {
      if (reply.succeeded()) {

        context.json(
          new JsonObject()
            .put("resursaAdaugata", reply.result().body())
        );

      } else {
        context.fail(reply.cause());
      }
    });

  }

  private void toateTransporturile(RoutingContext context) {
    DeliveryOptions options = new DeliveryOptions();
    JsonObject message = new JsonObject();
    vertx.eventBus().request(LIST_ALL_TRANSPORTURI_ADDR, message, options, reply -> {
      if (reply.succeeded()) {
        // The data from the db
        JsonArray body = (JsonArray) reply.result().body();

//        context.response()
//          .setChunked(true)
//          .setStatusCode(200)
//          .end(body.toBuffer());

        context.json(
          new JsonObject()
            .put("transporturi", body)
        );

      } else {
        context.fail(reply.cause());
      }
    });

  }

  private void updateTransport(RoutingContext context) {
    DeliveryOptions options = new DeliveryOptions();

    vertx.eventBus().request(UPDATE_TRANSPORT_ADDR, context.getBodyAsJson(), options, reply -> {
      if (reply.succeeded()) {

        context.json(
          new JsonObject()
            .put("resursaModificta", reply.result().body())
        );

      } else {
        context.fail(reply.cause());
      }
    });
  }
}
