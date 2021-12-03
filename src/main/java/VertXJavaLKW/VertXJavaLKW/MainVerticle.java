package VertXJavaLKW.VertXJavaLKW;

import controller.TransporturiController;
import handler.TransporturiHandler;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.util.List;

public class MainVerticle extends AbstractVerticle {

  final JsonObject loadedConfig = new JsonObject();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    doConfig()
      .compose(this::storeConfig)
      .compose(this::deployOtherVerticles)
      .onComplete(startPromise);

//    Route handler1 = router
//            .get("/transporturi")
//            .handler(TransporturiController::getTransporturi);

    // Mount the handler for all incoming requests at every path and HTTP method
//    router.route().handler(context -> {
//      // Get the address of the request
//      String address = context.request().connection().remoteAddress().toString();
//      // Get the query parameter "name"
//      MultiMap queryParams = context.queryParams();
//      String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
//      // Write a json response
//      context.json(
//        new JsonObject()
//          .put("name", name)
//          .put("address", address)
//          .put("message", "Hello " + name + " connected from " + address)
//      );
//    });
    // Create the HTTP server
//    vertx.createHttpServer()
//      // Handle every request using the router
//      .requestHandler(router)
//      // Start listening
//      .listen(8888)
//      // Print the port
//      .onSuccess(server ->
//        System.out.println(
//          "HTTP server started on port " + server.actualPort()
//        )
//      );

  }

  /**
   * Set up and execute the {@link ConfigRetriever} to load the configuration for the application
   */
  Future<JsonObject> doConfig() {
    ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
      .setType("file")
      .setFormat("json")
      .setConfig(new JsonObject().put("path", "config.json"));
    ConfigStoreOptions cliConfig = new ConfigStoreOptions()
      .setType("json")
      .setConfig(config());

    ConfigRetrieverOptions opts = new ConfigRetrieverOptions()
      .addStore(defaultConfig)
      .addStore(cliConfig);

    ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, opts);

    return Future.future(cfgRetriever::getConfig);
  }

  Future<Void> storeConfig(JsonObject config) {
    Promise<Void> promise = Promise.promise();
    loadedConfig.mergeIn(config);
    promise.complete();

    return promise.future();
  }

  /**
   * Deploy our other {@link io.vertx.core.Verticle}s in concurrently
   * https://vertx.io/docs/vertx-core/java/#_concurrent_composition
   * @param unused A {@link Void} instance (Not used in this method)
   * @return A {@link Future} which is resolved once both of the Verticles are deployed
   */
  Future<Void> deployOtherVerticles(Void unused) {
    Promise<Void> v1Promise = Promise.promise();
    v1Promise.complete();
    DeploymentOptions opts = new DeploymentOptions().setConfig(loadedConfig);

//    Future<String> dbVerticle = Future.future(promise -> vertx.deployVerticle(new DatabaseVerticle(), opts, promise));
    Future<String> webVerticle = Future.future(promise -> vertx.deployVerticle(new WebVerticle(), opts, promise));

    return CompositeFuture.all(webVerticle, v1Promise.future()).mapEmpty();
  }

//  @Override
//  public void stop(Promise<Void> stopPromise) throws Exception {
//    vertx.close();
//    System.out.println("stopping...");
//  }
}
