import io.vertx.ceylon.platform {
  Verticle,
  Container
}
import io.vertx.ceylon.core {
  Vertx
}
shared class StaticData() extends Verticle() {
  
  shared actual void start(Vertx vertx, Container container) {
    print("Importing static data");
  }  
}