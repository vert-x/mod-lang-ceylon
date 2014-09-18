import io.vertx.ceylon.platform { Verticle, Container }
import io.vertx.ceylon.core { Vertx }

shared class VerticleImpl() extends Verticle() {
  shared actual void start(Vertx vertx, Container container) {
    assert(container.env.size > 0);
    for (key->item in container.env) {
      assert(exists v = container.env[key]);
      assert(item == v);
    }
  }
}