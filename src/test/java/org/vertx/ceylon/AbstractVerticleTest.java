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
public abstract class AbstractVerticleTest {

  public void assertDeploy(String modulePath) throws Exception {
    AsyncResult<String> result = deploy(modulePath);
    if (result.failed()) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(result.cause());
      throw afe;
    }
  }

  public AsyncResult<String> deploy(String modulePath) throws Exception {
    File systemRepo = new File("target/system-repo");
    assertTrue(systemRepo.isDirectory());
    assertTrue(systemRepo.exists());
    System.setProperty("vertx.langs.ceylon", "io.vertx~lang-ceylon~1.0.0-SNAPSHOT:org.vertx.ceylon.platform.impl.CeylonVerticleFactory");
    PlatformManager manager = PlatformLocator.factory.createPlatformManager();
    final ArrayBlockingQueue<AsyncResult<String>> queue = new ArrayBlockingQueue<AsyncResult<String>>(10);
    manager.deployModuleFromClasspath("io.vertx~lang-ceylon~1.0.0-SNAPSHOT", new JsonObject(), 1, new URL[0], new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        queue.add(result);
      }
    });
    AsyncResult<String> result = queue.poll(30, TimeUnit.SECONDS);
    if (result.failed()) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(result.cause());
      throw afe;
    }
    manager.deployVerticle(modulePath, new JsonObject().putString("systemRepo", "flat:" + systemRepo.getAbsolutePath()), new URL[0], 1, null, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        queue.add(result);
      }
    });
    return queue.poll(30, TimeUnit.SECONDS);
  }
}
