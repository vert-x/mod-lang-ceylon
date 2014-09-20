import io.vertx.ceylon.platform { Verticle, Container }
import ceylon.json { JsonObject=Object }
import io.vertx.ceylon.core { Vertx }
import ceylon.promise { Promise }

shared class VerticleImpl1() extends Verticle() {
  shared actual Promise<Anything> asyncStart(Vertx vertx, Container container) {
    return container.deployVerticle {
      main = "ceylon:deployerverticle/1.0.0";
      conf = JsonObject { "main"->"deployerverticle::VerticleImpl2" };
    };
  }
}