package org.vertx.ceylon.platform.impl;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;
import org.vertx.java.platform.VerticleFactory;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CeylonVerticleFactory implements VerticleFactory {

  private Vertx vertx;
  private Container container;
  private ClassLoader cl;

  @Override
  public void init(Vertx vertx, Container container, ClassLoader cl) {
    this.cl = cl;
    this.container = container;
    this.cl = cl;
    this.vertx = vertx;
  }

  @Override
  public Verticle createVerticle(String main) throws Exception {
    CeylonVerticle verticle = new CeylonVerticle(main, cl);
    verticle.setVertx(vertx);
    verticle.setContainer(container);
    return verticle;
  }

  @Override
  public void reportException(Logger logger, Throwable t) {
    logger.error("Exception in Ceylon verticle", t);
  }

  @Override
  public void close() {

  }
}
