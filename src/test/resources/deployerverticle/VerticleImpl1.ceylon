import io.vertx.ceylon.platform { Verticle, Container }
import ceylon.json { JsonObject=Object }
import io.vertx.ceylon.core { Vertx }
import ceylon.promise { Promise }

shared class VerticleImpl1() extends Verticle() {
  shared actual Promise<Anything> asyncStart(Vertx vertx, Container container) {
    value cfg = container.config;
    assert(exists cfg);
    value main = cfg["_main"];
    assert(is String main);
    value conf = cfg["_conf"];
    assert(is JsonObject conf);
    return container.deployVerticle {
      main = main;
      conf = conf;
    };
  }
}