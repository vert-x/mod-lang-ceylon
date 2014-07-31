import org.vertx.java.core { VertxFactory { newVertx }, Vertx }

shared Vertx tester() {
    return newVertx();
}