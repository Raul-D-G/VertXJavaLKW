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

  /**
   * Store loaded configuration for use in subsequent operations
   * @param config The configuration loaded via Vert.x Config
   * @return A {@link Future} of type {@link Void} indication the success or failure of this operation
   */
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

    Future<String> dbVerticle = Future.future(promise -> vertx.deployVerticle(new DatabaseVerticle(), opts, promise));
    Future<String> webVerticle = Future.future(promise -> vertx.deployVerticle(new WebVerticle(), opts, promise));

    return CompositeFuture.all(webVerticle, dbVerticle).mapEmpty();
  }

}
