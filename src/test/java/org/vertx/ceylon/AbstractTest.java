package org.vertx.ceylon;

import org.junit.Before;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class AbstractTest {

  CeylonHelper compiler;
  VertxHelper helper;

  @Before
  public void before() {
    compiler = new CeylonHelper();
    helper = new VertxHelper();
  }
}
