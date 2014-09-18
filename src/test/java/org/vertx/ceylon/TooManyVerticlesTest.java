package org.vertx.ceylon;

import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class TooManyVerticlesTest extends AbstractTest {

  @Test
  public void testDeploy() throws Exception {
    VertxHelper vertx = new VertxHelper();
    vertx.assertFailedDeploy(DeployKind.VERTICLE, "toomanyverticles/module.ceylon");
  }
}
