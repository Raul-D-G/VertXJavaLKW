package VertXJavaLKW.VertXJavaLKW;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;

public class DatabaseVerticle extends AbstractVerticle {

  public static final String LIST_ALL_TRANSPORTURI_ADDR = "VertXJavaLKW.VertXJavaLKW.list_all_transporturi";
  public static final String ADD_TRANSPORT_ADD = "VertXJavaLKW.VertXJavaLKW.add_transport";
  public static final String GET_TRANSPORT_BY_ID_ADDR = "VertXJavaLKW.VertXJavaLKW.get_transport_by_id";
  public static final String UPDATE_TRANSPORT_ADDR = "VertXJavaLKW.VertXJavaLKW.update_transport";

  private static final String LIST_ALL_TRANSPORTURI = "SELECT * FROM transporturi ORDER BY pret ASC";
  private static final String GET_TRANSPORT_BY_ID = "SELECT * FROM transporturi WHERE id = ?";
  private static final String UPDATE_TRANSPORT = "UPDATE public.transporturi\n" +
    "\tSET \"idExpeditor\"=?, \"tipMarfa\"=?, \"taraIncarcare\"=?, \"orasIncarcare\"=?, \"taraDescarcare\"=?, \"orasDescarcare\"=?, pret=?, km=?\n" +
    "\tWHERE id = ? RETURNING *";
  private static final String ADD_TRANSPORT = "INSERT INTO public.transporturi(\n" +
    "\t\"idExpeditor\", \"tipMarfa\", \"taraIncarcare\", \"orasIncarcare\", \"taraDescarcare\", \"orasDescarcare\", pret, km)\n" +
    "\tVALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING *";

  SQLClient client;

  @Override
  public void start(Promise<Void> start) throws Exception {
      configureSqlClient()
      .compose(this::configureEventBusConsumers)
      .onComplete(start);
  }

  Future<Void> configureEventBusConsumers(Void unused) {
    Promise<Void> promise = Promise.promise();
    vertx.eventBus().consumer(LIST_ALL_TRANSPORTURI_ADDR).handler(this::listAllTransporturi);
    vertx.eventBus().consumer(GET_TRANSPORT_BY_ID_ADDR).handler(this::getTransportById);
    vertx.eventBus().consumer(UPDATE_TRANSPORT_ADDR).handler(this::updateTransport);
    vertx.eventBus().consumer(ADD_TRANSPORT_ADD).handler(this::addTransport);
    promise.complete();
    return promise.future();
  }

  void updateTransport(Message<Object> msg) {
    if (msg.body() instanceof JsonObject) {
      JsonObject transport = (JsonObject)msg.body();
      JsonArray params = new JsonArray()
        .add(transport.getInteger("idExpeditor"))
        .add(transport.getString("tipMarfa"))
        .add(transport.getString("taraIncarcare"))
        .add(transport.getString("orasIncarcare"))
        .add(transport.getString("taraDescarcare"))
        .add(transport.getString("orasDescarcare"))
        .add(transport.getFloat("pret"))
        .add(transport.getFloat("km"))
        .add(transport.getInteger("id"));
      Future.<SQLConnection>future(client::getConnection)
        .compose(conn -> this.queryWithParamters(conn, UPDATE_TRANSPORT, params))
        .compose(this::mapToFirstResult)
        .onComplete(res -> {
          if (res.succeeded()) {
            msg.reply(res.result());
          } else {
            msg.fail(500, res.cause().getLocalizedMessage());
          }
        });
    } else {
      msg.fail(400, "Bad Request: You must supply a valid Transport item in the body of the request");
    }
  }

  void addTransport(Message<Object> msg) {
    if (msg.body() instanceof JsonObject) {
      JsonObject transport = (JsonObject)msg.body();

      if (transport.containsKey("idExpeditor")) {
        JsonArray params = new JsonArray()
          .add(transport.getInteger("idExpeditor"))
          .add(transport.getString("tipMarfa"))
          .add(transport.getString("taraIncarcare"))
          .add(transport.getString("orasIncarcare"))
          .add(transport.getString("taraDescarcare"))
          .add(transport.getString("orasDescarcare"))
          .add(transport.getFloat("pret"))
          .add(transport.getFloat("km"));


        Future.<SQLConnection>future(client::getConnection)
          .compose(conn -> this.queryWithParamters(conn, ADD_TRANSPORT, params))
          .compose(this::mapToFirstResult)
          .onComplete(res -> {
            if (res.succeeded()) {
              msg.reply(res.result());
            } else {
              msg.fail(500, res.cause().getLocalizedMessage());
            }
          });
      } else {
        msg.fail(400, "Bad Request: Required field 'idExpeditor' missing from todo item.");
      }
    } else {
      msg.fail(400, "Bad Request: You must supply a valid Transport item in the body of the request");
    }
  }

  void getTransportById(Message<Object> msg) {
    if (msg.body() instanceof String) {
      Integer id = Integer.parseInt((String) msg.body());
      JsonArray params = new JsonArray().add(id);
      Future.<SQLConnection>future(client::getConnection)
        .compose(conn -> this.queryWithParamters(conn, GET_TRANSPORT_BY_ID, params))
        .compose(this::mapToFirstResult)
        .onComplete(res -> {
          if (res.succeeded()) {
            msg.reply(res.result());
          } else {
            msg.fail(500, res.cause().getLocalizedMessage());
          }
        });
    } else {
      msg.fail(400, "Bad Request, you must supply the Transport ID");
    }
  }

  void listAllTransporturi(Message<Object> msg) {
    Future.<SQLConnection>future(client::getConnection)
      .compose(conn -> this.queryWithParamters(conn, LIST_ALL_TRANSPORTURI, new JsonArray()))
      .compose(this::mapToJsonArray)
      .onComplete(res -> {
        if (res.succeeded()) {
          msg.reply(res.result());
        } else {
          msg.fail(500, res.cause().getLocalizedMessage());
        }
      });
  }

  Future<Void> configureSqlClient() {
    Promise<Void> promise = Promise.promise();
    client = JDBCClient.createShared(vertx, config().getJsonObject("db"));
    promise.complete();
    return promise.future();
  }

  Future<ResultSet> queryWithParamters(SQLConnection conn, String query, JsonArray params) {
    return Future.<ResultSet>future(promise -> conn.queryWithParams(query, params, promise));
  }

  Future<JsonObject> mapToFirstResult(ResultSet rs) {
    Promise<JsonObject> promise = Promise.promise();
    if (rs.getNumRows() >= 1) {
      promise.complete(rs.getRows().get(0));
      return promise.future();
    } else {
      promise.fail("Fara rezultate");
      return promise.future();
    }
  }

  Future<JsonArray> mapToJsonArray(ResultSet rs) {
    Promise<JsonArray> promise = Promise.promise();
    promise.complete(new JsonArray(rs.getRows()));
    return promise.future();
  }
}
