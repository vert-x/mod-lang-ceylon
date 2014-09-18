package org.vertx.ceylon;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class StartFailureVerticleTest extends AbstractTest {

  @Test
  public void testDeploy() throws Exception {
    VertxHelper vertx = new VertxHelper();
    Throwable t = vertx.assertFailedDeploy(DeployKind.VERTICLE, "startfailureverticle/module.ceylon");
    ceylon.language.Exception ex = (ceylon.language.Exception) t;
    assertEquals("it failed", ex.getMessage());
  }
}
