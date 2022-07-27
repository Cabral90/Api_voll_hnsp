package hnsp.voll.apiVoll;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;


import java.util.UUID;
import java.util.function.Function;

public class MainVerticle extends AbstractVerticle {


  private HttpServerResponse response;

  private PgPool pool;
  private Router router;
  private static final UUID getUUID = UUID.randomUUID();
  //private ValidateData validar = null;

  public static void main(String[] args) {

    Vertx.vertx().deployVerticle(new MainVerticle());
  }

  @Override
  public void start() { // Promise<Void> startPromise
    System.out.println(" Init.....");

    clientDB()

      .compose(this::setUpInitialData);

    System.out.println(" Init.....");

  }

  public Future<Void> setUpInitialData(Void vd) {
    System.out.println(" Init2 Router.....");

    RouterBuilder.create(vertx, "api.yaml")
      .onSuccess(routerBuilder -> {
        System.out.println(" Init3 call function .....");

        // call all endpoints
        // TODO: comprobar si hay sessione primero. caso contrario no se hara ningua operacion
   /*     this.haveSession()
          .compose(this::allFunction)
        .onFailure( " message error login");*/

        allFunction(routerBuilder);

        //Router router = Router.router(vertx)
        this.router = Router.router(vertx)
          .errorHandler(400, rc -> sendError(rc, 400, rc.failure().getMessage()));

        router.mountSubRouter("/v1", routerBuilder.createRouter());


        vertx
          .createHttpServer()
          .requestHandler(router)
          .listen(8080);
        System.out.println("pasamos");
      })
      .onFailure(Throwable::printStackTrace);
    return Future.succeededFuture();
  }

  private void allFunction(RouterBuilder router) {

    System.out.println("AQUI comenza la llamada de los componentes del API");
  }


  private Future<Void> clientDB() {
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("192.168.0.102")
      .setDatabase("chirpstack")
      .setUser("chirpstack_user")
      .setPassword("user3344");

// Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

// Create the client pool
    this.pool = PgPool.pool(vertx, connectOptions, poolOptions);
    //return this.pool;

    return Future.succeededFuture();
  }

  public static void sendError(
    final RoutingContext rc,
    final int code,
    final String cause) {

    final String message = HttpResponseStatus.valueOf(code).reasonPhrase();

    final JsonObject json = new JsonObject()
      .put("status", code)
      .put("title", message);

    if (cause != null && !cause.startsWith("ValidationException")) {
      json.put("cause", cause);
    }

    rc.response()
      .setStatusCode(code)
      .setStatusMessage(message)
      .putHeader("Content-Type", "application/json")
      .end(json.toBuffer());

  }



  /*
  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }*/
}
