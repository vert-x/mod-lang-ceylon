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
    VertxHelper vertx = new VertxHelper();
    assertNull(System.getProperty("lifecycle"));
    String deploymentId = vertx.assertDeploy(DeployKind.VERTICLE, "lifecycleverticle/module.ceylon");
    assertEquals("started", System.getProperty("lifecycle"));
    vertx.undeploy(deploymentId);
    assertEquals("stopped", System.getProperty("lifecycle"));
    System.clearProperty("lifecycle");
  }

  @Test
  public void testModuleFromUserRepository() throws Exception {
    VertxHelper vertx = new VertxHelper();
    assertNull(System.getProperty("lifecycle"));
    CeylonHelper ceylon = new CeylonHelper();
    ceylon.assertCompile("lifecycleverticle");
    String deploymentId = vertx.assertDeploy(DeployKind.VERTICLE, "ceylon:lifecycleverticle/1.0.0", new JsonObject().putString("userRepo", ceylon.modules.getCanonicalPath()));
    assertEquals("started", System.getProperty("lifecycle"));
    vertx.undeploy(deploymentId);
    assertEquals("stopped", System.getProperty("lifecycle"));
    System.clearProperty("lifecycle");
  }

  @Test
  public void testScript() throws Exception {
    VertxHelper vertx = new VertxHelper();
    assertNull(System.getProperty("lifecycle"));
    String deploymentId = vertx.assertDeploy(DeployKind.VERTICLE, "lifecycleverticle/VerticleImpl.ceylon");
    assertEquals("started", System.getProperty("lifecycle"));
    vertx.undeploy(deploymentId);
    assertEquals("stopped", System.getProperty("lifecycle"));
    System.clearProperty("lifecycle");
  }
}
