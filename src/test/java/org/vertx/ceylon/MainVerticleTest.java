package org.vertx.ceylon;

import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MainVerticleTest extends AbstractTest {

  @Test
  public void testDeploy() throws Exception {
    VertxHelper vertx = new VertxHelper();
    vertx.assertDeploy(DeployKind.VERTICLE, "mainverticle/module.ceylon");
  }
}
