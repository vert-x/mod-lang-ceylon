import io.vertx.ceylon.platform { Verticle, Container }
import io.vertx.ceylon.core { Vertx }

shared class VerticleImpl2() extends Verticle() {
  shared actual void start(Vertx vertx, Container container) {
    print("starting");
  }
}