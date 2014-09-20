package org.vertx.ceylon;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SpecifyVerticleTest extends AbstractTest {

  @Test
  public void testDeploy1() throws Exception {
    VertxHelper vertx = new VertxHelper();
    vertx.assertDeploy(
        DeployKind.VERTICLE,
        "toomanyverticles/module.ceylon",
        new JsonObject().putString("main", "toomanyverticles::VerticleImpl2"));
  }

  @Test
  public void testDeploy2() throws Exception {
    VertxHelper vertx = new VertxHelper();
    vertx.assertDeploy(
        DeployKind.VERTICLE,
        "mainverticle/module.ceylon",
        new JsonObject().putString("main", "mainverticle::VerticleImpl2"));
  }
}
