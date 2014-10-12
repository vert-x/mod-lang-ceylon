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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This must be executed in the integration-test phase as a Java main.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class IntegrationTester {

  private static final Logger log = Logger.getLogger(IntegrationTester.class.getSimpleName());

  public static void main(String[] args) throws Exception {
    // run("testDeploySelfFromCeylonVerticle");

    //
    run("testDeployModuleAsVerticle");

    // Zipped module
    run("testDeployCarAsZippedModule");
    run("testDeployModuleAsZippedModule");
    run("testDeploySourceAsZippedModule");

    // Module
    run("testDeployCarAsExplodedModule");
    run("testDeployModuleAsExplodedModule");
    run("testDeploySourceAsExplodedModule");

    // Nested deploy
    run("testDeployCarVerticleFromCeylonVerticle");
    run("testDeployJavaScriptVerticleFromCeylonVerticle");
    run("testDeploySourceVerticleFromSourceModule");
  }

  private static Object run(String methodName) throws Exception {
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
      Method test = fooClass.getMethod(methodName);
      log.info("Executing test " + methodName);
      return test.invoke(null);
    } finally {
      thread.setContextClassLoader(prevLoader);
    }
  }

  public static void testDeployModuleAsVerticle() throws Throwable {
    PlatformManager manager = createPlatform();
    System.clearProperty("lifecycle");
    try {
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

  public static void testDeployCarAsZippedModule() throws Throwable {
    assertFile(new File(Helper.assertModules(), "lifecycleverticle/1.0.0/lifecycleverticle-1.0.0.car"));
    final File zip = new File("target/testDeployCarAsZippedModule.zip");
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
    out.putNextEntry(new ZipEntry("mod.json"));
    String desc = "{ \"main\": \"ceylon:lifecycleverticle/1.0.0\" }";
    out.write(desc.getBytes());
    out.closeEntry();
    out.close();
    testDeployLifeCycle(new Deployment() {
      @Override
      public void deployTo(PlatformManager platform, Handler<AsyncResult<String>> handler) {
        platform.deployModuleFromZip(zip.getAbsolutePath(), new JsonObject().putString("userRepo", "target/modules"), 1, handler);
      }
    });
  }

  public static void testDeploySourceAsZippedModule() throws Throwable {
    File source = assertFile("src/test/resources/lifecycleverticle/VerticleImpl.ceylon");
    final File zip = new File("target/testDeploySourceAsZippedModule.zip");
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
    out.putNextEntry(new ZipEntry("mod.json"));
    String desc = "{ \"main\": \"VerticleImpl.ceylon\" }";
    out.write(desc.getBytes());
    out.closeEntry();
    out.putNextEntry(new ZipEntry("VerticleImpl.ceylon"));
    Files.copy(source.toPath(), out);
    out.closeEntry();
    out.close();
    testDeployLifeCycle(new Deployment() {
      @Override
      public void deployTo(PlatformManager platform, Handler<AsyncResult<String>> handler) {
        platform.deployModuleFromZip(zip.getAbsolutePath(), new JsonObject().putString("userRepo", "target/modules"), 1, handler);
      }
    });
  }

  public static void testDeployModuleAsZippedModule() throws Throwable {
    File verticle = assertFile("src/test/resources/lifecycleverticle/VerticleImpl.ceylon");
    File module = assertFile("src/test/resources/lifecycleverticle/module.ceylon");
    File pkg = assertFile("src/test/resources/lifecycleverticle/package.ceylon");
    final File zip = new File("target/testDeployModuleAsZippedModule.zip");
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
    out.putNextEntry(new ZipEntry("mod.json"));
    String desc = "{ \"main\": \"lifecycleverticle/module.ceylon\" }";
    out.write(desc.getBytes());
    out.closeEntry();
    out.putNextEntry(new ZipEntry("lifecycleverticle/"));
    out.closeEntry();
    out.putNextEntry(new ZipEntry("lifecycleverticle/VerticleImpl.ceylon"));
    Files.copy(verticle.toPath(), out);
    out.closeEntry();
    out.putNextEntry(new ZipEntry("lifecycleverticle/module.ceylon"));
    Files.copy(module.toPath(), out);
    out.closeEntry();
    out.putNextEntry(new ZipEntry("lifecycleverticle/package.ceylon"));
    Files.copy(pkg.toPath(), out);
    out.closeEntry();
    out.close();
    testDeployLifeCycle(new Deployment() {
      @Override
      public void deployTo(PlatformManager platform, Handler<AsyncResult<String>> handler) {
        platform.deployModuleFromZip(zip.getAbsolutePath(), new JsonObject().putString("userRepo", "target/modules"), 1, handler);
      }
    });
  }

  public static void testDeployCarAsExplodedModule() throws Throwable {
    assertFile(new File(Helper.assertModules(), "lifecycleverticle/1.0.0/lifecycleverticle-1.0.0.car"));
    File moduleDir = mkDirs("target/integration~testDeployCarAsExplodedModule~1.0.0");
    new FileWriter(new File(moduleDir, "mod.json")).append("{ \"main\": \"ceylon:lifecycleverticle/1.0.0\" }").close();
    testDeployLifeCycle(new Deployment() {
      @Override
      public void deployTo(PlatformManager platform, Handler<AsyncResult<String>> handler) throws Exception {
        platform.deployModule("integration~testDeployCarAsExplodedModule~1.0.0", new JsonObject().putString("userRepo", "target/modules"), 1, handler);
      }
    });
  }

  public static void testDeployModuleAsExplodedModule() throws Throwable {
    File verticle = assertFile("src/test/resources/lifecycleverticle/VerticleImpl.ceylon");
    File module = assertFile("src/test/resources/lifecycleverticle/module.ceylon");
    File pkg = assertFile("src/test/resources/lifecycleverticle/package.ceylon");
    File moduleDir = mkDirs("target/integration~testDeployModuleAsExplodedModule~1.0.0");
    new FileWriter(new File(moduleDir, "mod.json")).append("{ \"main\": \"lifecycleverticle/module.ceylon\" }").close();
    File dir = mkDirs(new File(moduleDir, "lifecycleverticle"));
    Files.copy(verticle.toPath(), new FileOutputStream(new File(dir, "VerticleImpl.ceylon")));
    Files.copy(module.toPath(), new FileOutputStream(new File(dir, "module.ceylon")));
    Files.copy(pkg.toPath(), new FileOutputStream(new File(dir, "package.ceylon")));
    testDeployLifeCycle(new Deployment() {
      @Override
      public void deployTo(PlatformManager platform, Handler<AsyncResult<String>> handler) throws Exception {
        platform.deployModule("integration~testDeployModuleAsExplodedModule~1.0.0", new JsonObject(), 1, handler);
      }
    });
  }

  public static void testDeploySourceAsExplodedModule() throws Throwable {
    File moduleDir = mkDirs("target/integration~testDeploySourceAsExplodedModule~1.0.0");
    new FileWriter(new File(moduleDir, "mod.json")).append("{ \"main\": \"VerticleImpl.ceylon\" }").close();
    File source = assertFile("src/test/resources/lifecycleverticle/VerticleImpl.ceylon");
    Files.copy(source.toPath(), new FileOutputStream(new File(moduleDir, "VerticleImpl.ceylon")));
    testDeployLifeCycle(new Deployment() {
      @Override
      public void deployTo(PlatformManager platform, Handler<AsyncResult<String>> handler) throws Exception {
        platform.deployModule("integration~testDeploySourceAsExplodedModule~1.0.0", new JsonObject(), 1, handler);
      }
    });
  }

  public static void testDeploySourceVerticleFromSourceModule() throws Throwable {
    File moduleDir = mkDirs("target/integration~testDeploySourceVerticleFromSourceModule~1.0.0");
    new FileWriter(new File(moduleDir, "mod.json")).append("{ \"main\": \"VerticleImpl1.ceylon\" }").close();
    File lifecycle = assertFile("src/test/resources/lifecycleverticle/VerticleImpl.ceylon");
    File deployer = assertFile("src/test/resources/deployerverticle/VerticleImpl1.ceylon");
    Files.copy(lifecycle.toPath(), new FileOutputStream(new File(moduleDir, "VerticleImpl.ceylon")));
    Files.copy(deployer.toPath(), new FileOutputStream(new File(moduleDir, "VerticleImpl1.ceylon")));
    testDeployLifeCycle(new Deployment() {
      @Override
      public void deployTo(PlatformManager platform, Handler<AsyncResult<String>> handler) throws Exception {
        platform.deployModule("integration~testDeploySourceVerticleFromSourceModule~1.0.0", new JsonObject().
            putString("_main", "VerticleImpl.ceylon").
            putObject("_conf", new JsonObject()), 1, handler);
      }
    });
  }

  interface Deployment {
    void deployTo(PlatformManager platform, Handler<AsyncResult<String>> handler) throws Exception;
  }

  private static void testDeployLifeCycle(Deployment deployment) throws Throwable {
    PlatformManager manager = createPlatform();
    System.clearProperty("lifecycle");
    try {
      final ArrayBlockingQueue<AsyncResult<String>> queue = new ArrayBlockingQueue<>(10);
      deployment.deployTo(manager, new Handler<AsyncResult<String>>() {
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

  public static void testDeploySelfFromCeylonVerticle() throws Throwable {
    assertFile(new File(Helper.assertModules(), "deployerverticle/1.0.0/deployerverticle-1.0.0.car"));
    String userRepo = Helper.assertModules().getAbsolutePath();
    testDeployVerticleFromVerticle(new JsonObject().
        putString("userRepo", userRepo).
        putString("_main", "ceylon:deployerverticle/1.0.0").
        putObject("_conf", new JsonObject().
            putString("main", "deployerverticle::VerticleImpl2").
            putString("userRepo", userRepo)));
  }

  public static void testDeployCarVerticleFromCeylonVerticle() throws Throwable {
    assertFile(new File(Helper.assertModules(), "deployerverticle/1.0.0/deployerverticle-1.0.0.car"));
    String userRepo = Helper.assertModules().getAbsolutePath();
    testDeployVerticleFromVerticle(new JsonObject().
        putString("userRepo", userRepo).
        putString("_main", "ceylon:lifecycleverticle/1.0.0").
        putObject("_conf", new JsonObject().
            putString("userRepo", userRepo)));
  }

  public static void testDeployJavaScriptVerticleFromCeylonVerticle() throws Throwable {
    assertFile(new File(Helper.assertModules(), "deployerverticle/1.0.0/deployerverticle-1.0.0.car"));
    String userRepo = Helper.assertModules().getAbsolutePath();
    testDeployVerticleFromVerticle(new JsonObject().
        putString("userRepo", userRepo).
        putString("_main", "deployerverticle/verticle.js").
        putObject("_conf", new JsonObject()));
  }

  private static void testDeployVerticleFromVerticle(JsonObject conf) throws Throwable {
    PlatformManager manager = createPlatform();
    try {
      final ArrayBlockingQueue<AsyncResult<String>> queue = new ArrayBlockingQueue<>(10);
      manager.deployVerticle(
          "ceylon:deployerverticle/1.0.0",
          conf,
          new URL[0],
          1,
          null,
          new Handler<AsyncResult<String>>() {
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

  private static PlatformManager createPlatform() throws Throwable {
    File target = assertDir(new File("target"));
    System.setProperty("vertx.mods", target.getAbsolutePath());
    System.setProperty("vertx.langs.ceylon", "io.vertx~lang-ceylon~" + System.getProperty("project.version") + ":org.vertx.ceylon.platform.impl.CeylonVerticleFactory");
    System.setProperty("org.vertx.logger-delegate-factory-class-name", LogDelegateFactoryImpl.class.getName());
    return PlatformLocator.factory.createPlatformManager();
  }

  private static File mkDirs(String name) {
    return mkDirs(new File(name));
  }

  private static File mkDirs(File dir) {
    assertTrue(dir.mkdirs());
    return dir;
  }

  private static File assertFile(String name) {
    return assertFile(new File(name));
  }

  private static File assertFile(File f) {
    assertTrue(f.exists());
    assertTrue(f.isFile());
    return f;
  }

  private static File assertDir(File f) {
    assertTrue(f.exists());
    assertTrue(f.isDirectory());
    return f;
  }
}
