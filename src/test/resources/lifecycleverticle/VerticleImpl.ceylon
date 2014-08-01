import io.vertx.ceylon.platform {
	Verticle,
  Container
}
import io.vertx.ceylon {
  Vertx
}
import java.lang { System { setProperty } }


shared class VerticleImpl() extends Verticle() {
	
	shared actual void start(Vertx vertx, Container container) {
		setProperty("lifecycle", "started");
	}
	
	shared actual void stop() {
		setProperty("lifecycle", "stopped");
	}
}