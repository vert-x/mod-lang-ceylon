import io.vertx.ceylon.core {
  Vertx
}
import ceylon.promise {
  Promise,
  Deferred
}

"""A verticle is the unit of execution in the Vert.x platform
   Vert.x code is packaged into Verticle's and then deployed and executed by the Vert.x platform.
   Verticles can be written in different languages.
"""
shared abstract class Verticle() {
  
  shared default Promise<Anything> asyncStart(Vertx vertx, Container container) {
    value deferred = Deferred<Anything>();
    try {
      start(vertx, container);
      deferred.fulfill("started"); // Cannot fulfull with null => bug
    } catch (Throwable t) {
      deferred.reject(t);
    }
    return deferred.promise;
  }
  
  shared default void start(Vertx vertx, Container container) {
  }
  
  shared default void stop() {
  }
}
