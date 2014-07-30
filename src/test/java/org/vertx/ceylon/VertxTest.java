package org.vertx.ceylon;

import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class VertxTest {

  @Test
  public void testDeploy() throws Exception {

    //
    File systemRepo = new File("target/system-repo");
    assertTrue(systemRepo.isDirectory());
    assertTrue(systemRepo.exists());
    System.setProperty("vertx.langs.ceylon", "io.vertx~lang-ceylon~1.0.0-SNAPSHOT:org.vertx.ceylon.platform.impl.CeylonVerticleFactory");
    PlatformManager manager = PlatformLocator.factory.createPlatformManager();
    final ArrayBlockingQueue<AsyncResult<String>> queue = new ArrayBlockingQueue<AsyncResult<String>>(10);

    //
    manager.deployModuleFromClasspath("io.vertx~lang-ceylon~1.0.0-SNAPSHOT", new JsonObject(), 1, new URL[0], new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        queue.add(result);
      }
    });
    AsyncResult<String> a = queue.poll(10, TimeUnit.SECONDS);
    if (a.failed()) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(a.cause());
      throw afe;
    }

    //
    manager.deployVerticle("helloworld/module.ceylon", new JsonObject().putString("systemRepo", systemRepo.getAbsolutePath()), new URL[0], 1, null, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        queue.add(result);
      }
    });
    a = queue.poll(10, TimeUnit.SECONDS);
    if (a.failed()) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(a.cause());
      throw afe;
    }
  }
}
