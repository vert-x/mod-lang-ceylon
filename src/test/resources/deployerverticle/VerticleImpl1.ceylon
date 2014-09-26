import io.vertx.ceylon.platform { Verticle, Container }
import ceylon.json { JsonObject=Object }
import io.vertx.ceylon.core { Vertx }
import ceylon.promise { Promise }

shared class VerticleImpl1() extends Verticle() {
  shared actual Promise<Anything> asyncStart(Vertx vertx, Container container) {
    value cfg = container.config;
    assert(exists cfg);
    value userRepo = cfg["userRepo"];
    assert(is String userRepo);
    return container.deployVerticle {
      main = "ceylon:deployerverticle/1.0.0";
      conf = JsonObject { "main"->"deployerverticle::VerticleImpl2", "userRepo"->userRepo };
    };
  }
}