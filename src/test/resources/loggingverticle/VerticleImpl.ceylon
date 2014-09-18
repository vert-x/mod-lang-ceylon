import io.vertx.ceylon.platform { Verticle, Container }
import io.vertx.ceylon.core { Vertx }

shared class VerticleImpl() extends Verticle() {
  shared actual void start(Vertx vertx, Container container) {
    container.logger.info("hello world");
  }
}