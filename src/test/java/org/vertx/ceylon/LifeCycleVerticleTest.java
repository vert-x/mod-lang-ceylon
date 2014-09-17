package org.vertx.ceylon;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class LifeCycleVerticleTest extends AbstractTest {

  @Test
  public void testModuleFromSources() throws Exception {
    assertNull(System.getProperty("lifecycle"));
    String deploymentId = helper.assertDeploy(DeployKind.VERTICLE, "lifecycleverticle/module.ceylon");
    assertEquals("started", System.getProperty("lifecycle"));
    helper.undeploy(deploymentId);
    assertEquals("stopped", System.getProperty("lifecycle"));
    System.clearProperty("lifecycle");
  }

  @Test
  public void testModuleFromUserRepository() throws Exception {
    assertNull(System.getProperty("lifecycle"));
    compiler.assertCompile("lifecycleverticle");
    String deploymentId = helper.assertDeploy(DeployKind.VERTICLE, "ceylon:lifecycleverticle/1.0.0", new JsonObject().putString("userRepo", compiler.modules.getCanonicalPath()));
    assertEquals("started", System.getProperty("lifecycle"));
    helper.undeploy(deploymentId);
    assertEquals("stopped", System.getProperty("lifecycle"));
    System.clearProperty("lifecycle");
  }

  @Test
  public void testScript() throws Exception {
    assertNull(System.getProperty("lifecycle"));
    String deploymentId = helper.assertDeploy(DeployKind.VERTICLE, "lifecycleverticle/VerticleImpl.ceylon");
    assertEquals("started", System.getProperty("lifecycle"));
    helper.undeploy(deploymentId);
    assertEquals("stopped", System.getProperty("lifecycle"));
    System.clearProperty("lifecycle");
  }
}
