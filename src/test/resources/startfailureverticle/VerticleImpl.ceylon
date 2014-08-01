import io.vertx.ceylon.platform {
	Verticle,
  Container
}
import io.vertx.ceylon {
  Vertx
}


shared class VerticleImpl() extends Verticle() {

	shared actual void start(Vertx vertx, Container container) {
	    throw Exception("it failed");
	}

}