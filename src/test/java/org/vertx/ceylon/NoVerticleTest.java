package org.vertx.ceylon;

import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class NoVerticleTest extends AbstractVerticleTest {

  @Test
  public void testDeploy() throws Exception {
    assertFailedDeploy("noverticle/module.ceylon");
  }
}
