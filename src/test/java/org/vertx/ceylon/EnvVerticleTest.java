package org.vertx.ceylon;

import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class EnvVerticleTest extends AbstractTest {

  @Test
  public void testDeploy() throws Exception {
    VertxHelper vertx = new VertxHelper();
    String deploymentId = vertx.assertDeploy(DeployKind.VERTICLE, "envverticle/module.ceylon");
    vertx.undeploy(deploymentId);
  }
}
