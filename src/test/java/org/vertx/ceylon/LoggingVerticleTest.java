package org.vertx.ceylon;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class LoggingVerticleTest extends AbstractTest {

  @Test
  public void testDeploy() throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    PrintStream old = System.out;
    System.setOut(new PrintStream(buffer));
    try {
      VertxHelper vertx = new VertxHelper();
      String deploymentId = vertx.assertDeploy(DeployKind.VERTICLE, "loggingverticle/module.ceylon");
      vertx.undeploy(deploymentId);
      String s = buffer.toString();
      assertTrue("Was expecting to find <[info] hello world> in log <" + s + ">", s.contains("[info] hello world"));
    } finally {
      System.setOut(old);
    }
  }
}
