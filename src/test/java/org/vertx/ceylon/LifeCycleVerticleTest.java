package org.vertx.ceylon;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class LifeCycleVerticleTest extends AbstractVerticleTest {

  @Test
  public void testModuleFromSources() throws Exception {
    assertNull(System.getProperty("lifecycle"));
    String deploymentId = assertDeploy("lifecycleverticle/module.ceylon");
    assertEquals("started", System.getProperty("lifecycle"));
    undeploy(deploymentId);
    assertEquals("stopped", System.getProperty("lifecycle"));
    System.clearProperty("lifecycle");
  }

  @Test
  public void testModuleFromUserRepository() throws Exception {
    assertNull(System.getProperty("lifecycle"));
    assertCompile("lifecycleverticle");
    String deploymentId = assertDeploy("ceylon:lifecycleverticle/1.0.0", new JsonObject().putString("userRepo", modules.getCanonicalPath()));
    assertEquals("started", System.getProperty("lifecycle"));
    undeploy(deploymentId);
    assertEquals("stopped", System.getProperty("lifecycle"));
    System.clearProperty("lifecycle");
  }

  @Test
  public void testScript() throws Exception {
    assertNull(System.getProperty("lifecycle"));
    String deploymentId = assertDeploy("lifecycleverticle/VerticleImpl.ceylon");
    assertEquals("started", System.getProperty("lifecycle"));
    undeploy(deploymentId);
    assertEquals("stopped", System.getProperty("lifecycle"));
    System.clearProperty("lifecycle");
  }
}
