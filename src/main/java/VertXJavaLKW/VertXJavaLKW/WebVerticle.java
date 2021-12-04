package VertXJavaLKW.VertXJavaLKW;

import handler.TransporturiHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class WebVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> start) throws Exception {
    configureRouter()
      .compose(this::startHttpServer)
      .onComplete(start);
  }

  /**
   * Configures the {@link Router} for use in handling HTTP requests in the server
   *
   * @return A {@link Future}, potentially containing the {@link Router}, if this succeeds
   */
  Future<Router> configureRouter() {
    Promise<Router> promise = Promise.promise();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router
      .route("/transporturi/*")
      .handler(TransporturiHandler.create(vertx, router));
    promise.complete(router);
    return promise.future();
  }

  /**
   * Using the provided {@link Router}, start and {@link HttpServer} and use the router as the handler
   *
   * @return A {@link Future} which will contain the {@link HttpServer} on successful creation of the server
   */
  Future<Void> startHttpServer(Router router) {
    JsonObject http = config().getJsonObject("http");
    int httpPort = http.getInteger("port");
    HttpServer server = vertx.createHttpServer().requestHandler(router);
    System.out.println("Server starter on port " + httpPort);
    return Future.<HttpServer>future(promise -> server.listen(httpPort, promise)).mapEmpty();
  }


}
