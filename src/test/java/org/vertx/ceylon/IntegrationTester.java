package org.vertx.ceylon;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.EventLoop;
import org.junit.Assert;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This must be executed in the integration-test phase as a Java main.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class IntegrationTester {

  public static void main(String[] args) throws Exception {
    Thread thread = Thread.currentThread();
    ClassLoader prevLoader = thread.getContextClassLoader();
    try {
      ClassLoader loader = new URLClassLoader(new URL[]{
          Assert.class.getProtectionDomain().getCodeSource().getLocation(),
          IntegrationTester.class.getProtectionDomain().getCodeSource().getLocation(),
          EventLoop.class.getProtectionDomain().getCodeSource().getLocation(),
          Versioned.class.getProtectionDomain().getCodeSource().getLocation(),
          ObjectMapper.class.getProtectionDomain().getCodeSource().getLocation(),
          JsonAutoDetect.class.getProtectionDomain().getCodeSource().getLocation(),
          Vertx.class.getProtectionDomain().getCodeSource().getLocation(),
          PlatformManager.class.getProtectionDomain().getCodeSource().getLocation(),
      }, ClassLoader.getSystemClassLoader());
      thread.setContextClassLoader(loader);
      Class<?> fooClass = loader.loadClass(IntegrationTester.class.getName());
      Method test = fooClass.getMethod("testModuleFromSources");
      test.invoke(null);
    } finally {
      thread.setContextClassLoader(prevLoader);
    }
  }

  public static void testModuleFromSources() throws Throwable {
    File modZip = new File("target");
    assertTrue(modZip.exists());
    assertTrue(modZip.isDirectory());
    System.setProperty("vertx.mods", modZip.getAbsolutePath());
    System.setProperty("vertx.langs.ceylon", "io.vertx~lang-ceylon~1.0.0-alpha3-SNAPSHOT:org.vertx.ceylon.platform.impl.CeylonVerticleFactory");
    System.setProperty("org.vertx.logger-delegate-factory-class-name", LogDelegateFactoryImpl.class.getName());
    PlatformManager manager = PlatformLocator.factory.createPlatformManager();
    try {
      assertNull(System.getProperty("lifecycle"));
      final ArrayBlockingQueue<AsyncResult<String>> queue = new ArrayBlockingQueue<>(10);
      manager.deployVerticle("lifecycleverticle/module.ceylon", new JsonObject(), new URL[0], 1, null, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          queue.add(result);
        }
      });
      AsyncResult<String> result = queue.poll(30, TimeUnit.SECONDS);
      if (!result.succeeded()) {
        throw result.cause();
      }
      assertEquals("started", System.getProperty("lifecycle"));
      final CountDownLatch latch = new CountDownLatch(1);
      manager.undeploy(result.result(), new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> event) {
          latch.countDown();
        }
      });
      latch.await();
      assertEquals("stopped", System.getProperty("lifecycle"));
    } finally {
      manager.stop();
    }
  }
}
