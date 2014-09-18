package org.vertx.ceylon;

import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class NoVerticleTest extends AbstractTest {

  @Test
  public void testDeploy() throws Exception {
    VertxHelper vertx = new VertxHelper();
    vertx.assertFailedDeploy(DeployKind.VERTICLE, "noverticle/module.ceylon");
  }
}
