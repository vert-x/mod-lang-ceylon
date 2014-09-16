package org.vertx.ceylon;

import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class UncompilableVerticleTest extends AbstractTest {

  @Test
  public void testDeploy() throws Exception {
    helper.assertFailedDeploy("uncompilableverticle/module.ceylon");
  }
}
