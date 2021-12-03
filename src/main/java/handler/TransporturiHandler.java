package handler;

import controller.TransporturiController;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public interface TransporturiHandler extends Handler<RoutingContext> {
  static TransporturiHandler create(Vertx vertx, Router router) {
    return new TransporturiController(vertx, router);
  }
}
