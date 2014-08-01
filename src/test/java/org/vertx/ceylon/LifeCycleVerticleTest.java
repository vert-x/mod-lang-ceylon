package org.vertx.ceylon;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class LifeCycleVerticleTest extends AbstractVerticleTest {

  @Test
  public void testDeploy() throws Exception {
    assertNull(System.getProperty("lifecycle"));
    String deploymentId = assertDeploy("lifecycleverticle/module.ceylon");
    assertEquals("started", System.getProperty("lifecycle"));
    undeploy(deploymentId);
    assertEquals("stopped", System.getProperty("lifecycle"));
    System.clearProperty("lifecycle");
  }
}
