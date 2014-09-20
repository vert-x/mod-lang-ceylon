package org.vertx.ceylon;

import com.redhat.ceylon.compiler.java.runtime.metamodel.Metamodel;
import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunner;
import com.redhat.ceylon.compiler.java.runtime.tools.RunnerOptions;
import org.junit.Test;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Verticle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class BasicTest extends AbstractTest {

  @Test
  public void testCompile() {
    CeylonHelper ceylon = new CeylonHelper();
    ceylon.assertCompile("helloworld");
    JavaRunner runner = ceylon.runner("helloworld", "1.0.0");
    runner.run();
    runner.cleanup();
    Metamodel.resetModuleManager();
  }

  @Test
  public void testSDK() {
    CeylonHelper ceylon = new CeylonHelper();
    ceylon.assertCompile("sdk");
    JavaRunner runner = ceylon.runner("sdk", "1.0.0");
    runner.run();
    runner.cleanup();
    Metamodel.resetModuleManager();
  }

  @Test
  public void testOverride() throws Exception {
    CeylonHelper ceylon = new CeylonHelper();
    ceylon.assertCompile("helloworld");
    ceylon.assertCompile("override");
    RunnerOptions options = new RunnerOptions();
    options.addExtraModule("helloworld", "1.0.0");
    JavaRunner runner = ceylon.runner(options, "override", "1.0.0");
    runner.run();
    ClassLoader loader = runner.getModuleClassLoader();
    Class<?> vertxClass = loader.loadClass(Vertx.class.getName());
    assertEquals(Vertx.class, vertxClass);
    Vertx vertx = (Vertx) loader.loadClass("override.tester_").getDeclaredMethod("tester").invoke(null);
    ArrayList<String> p = (ArrayList<String>) loader.loadClass("override.lister_").getDeclaredMethod("lister").invoke(null);
    assertTrue(p.contains("helloworld"));
    runner.cleanup();
    Metamodel.resetModuleManager();
  }

  @Test
  public void testVerticleDiscovery() throws Exception {
    CeylonHelper ceylon = new CeylonHelper();
    ceylon.assertCompile("noopverticle");
    RunnerOptions options = new RunnerOptions();
    options.addExtraModule("noopverticle", "1.0.0");
    options.addUserRepository(Helper.assertVertxRepo().getAbsolutePath());
    JavaRunner runner = ceylon.runner(options, "io.vertx.ceylon.platform", "0.4.0");
    runner.run();
    ClassLoader loader = runner.getModuleClassLoader();
    Method findVerticlesMethod = loader.loadClass("io.vertx.ceylon.platform.findVerticles_").getDeclaredMethod("findVerticles", String.class, String.class);
    Map<String, Callable<Verticle>> factories = (Map<String, Callable<Verticle>>) findVerticlesMethod.invoke(null, "noopverticle", null);
    assertEquals(1, factories.size());
    Verticle verticle = factories.values().iterator().next().call();
    assertTrue(Verticle.class.isInstance(verticle));
    runner.cleanup();
    Metamodel.resetModuleManager();
  }
}
