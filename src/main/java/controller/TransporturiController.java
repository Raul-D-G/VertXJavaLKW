package controller;

import handler.TransporturiHandler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
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
  }


  @Override
  public void handle(final RoutingContext context) {
    router.handleContext(context);
  }

  private void getTransport(RoutingContext context) {

    //Option A: do you business logic here
  }

  private void inregistrareTransport(RoutingContext context) {

  }

  private void toateTransporturile(RoutingContext context) {


    vertx.eventBus().send(LIST_ALL_TRANSPORTURI_ADDR,new JsonObject());

  }
}
