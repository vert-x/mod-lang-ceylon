package org.vertx.ceylon;

import junit.framework.AssertionFailedError;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.impl.LogDelegateFactory;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.PrintStream;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class VertxHelper {

  private PlatformManager manager;

  public VertxHelper() {
  }

  private PlatformManager getManager() throws Exception {
    if (manager == null) {
      System.setProperty("vertx.langs.ceylon", "io.vertx~lang-ceylon~1.0.0-alpha-SNAPSHOT:org.vertx.ceylon.platform.impl.CeylonVerticleFactory");
      PlatformManager manager = PlatformLocator.factory.createPlatformManager();
      System.setProperty("org.vertx.logger-delegate-factory-class-name", LogDelegateFactoryImpl.class.getName());
      LoggerFactory.initialise();
      final ArrayBlockingQueue<AsyncResult<String>> queue = new ArrayBlockingQueue<AsyncResult<String>>(10);
      manager.deployModuleFromClasspath("io.vertx~lang-ceylon~1.0.0-alpha-SNAPSHOT", new JsonObject(), 1, new URL[0], new Handler<AsyncResult<String>>() {
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
      this.manager = manager;
    }
    return manager;
  }

  public String assertDeploy(DeployKind deployKind, String path) throws Exception {
    return assertDeploy(deployKind, path, new JsonObject());
  }

  public String assertDeploy(DeployKind deployKind, String path, JsonObject config) throws Exception {
    AsyncResult<String> result = deploy(deployKind, path, config);
    if (result.failed()) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(result.cause());
      throw afe;
    } else {
      return result.result();
    }
  }

  public Throwable assertFailedDeploy(DeployKind deployKind, String path) throws Exception {
    return assertFailedDeploy(deployKind, path, new JsonObject());
  }

  public Throwable assertFailedDeploy(DeployKind deployKind, String path, JsonObject config) throws Exception {
    AsyncResult<String> result = deploy(deployKind, path, config);
    if (result.succeeded()) {
      throw new AssertionFailedError("Was expecting deployment of " + path + " to fail");
    } else {
      return result.cause();
    }
  }

  public AsyncResult<String> deploy(DeployKind deployKind, String path, JsonObject config) throws Exception {
    if (!config.containsField("systemRepo")) {
      config.putString("systemRepo", "flat:" + Helper.assertSystemRepo().getAbsolutePath());
    }
    if (!config.containsField("vertxRepo")) {
      config.putString("vertxRepo", Helper.assertVertxRepo().getAbsolutePath());
    }
    final ArrayBlockingQueue<AsyncResult<String>> queue = new ArrayBlockingQueue<AsyncResult<String>>(10);
    Handler<AsyncResult<String>> done = new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        queue.add(result);
      }
    };
    switch (deployKind) {
      case MOD_ZIP:
        getManager().deployModuleFromZip(path, config, 1, done);
        break;
      case VERTICLE:
        getManager().deployVerticle(path, config, new URL[0], 1, null, done);
        break;
    }
    return queue.poll(30, TimeUnit.SECONDS);
  }

  public void undeploy(String deploymentId) throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    getManager().undeploy(deploymentId, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> event) {
        latch.countDown();
      }
    });
    latch.await(30, TimeUnit.SECONDS);
  }

}
