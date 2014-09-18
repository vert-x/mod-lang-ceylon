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
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class BasicTest extends AbstractTest {

  @Test
  public void testCompile() {
    compiler.assertCompile("helloworld");
    JavaRunner runner = compiler.runner("helloworld", "1.0.0");
    runner.run();
    runner.cleanup();
    Metamodel.resetModuleManager();
  }

  @Test
  public void testSDK() {
    compiler.assertCompile("sdk");
    JavaRunner runner = compiler.runner("sdk", "1.0.0");
    runner.run();
    runner.cleanup();
    Metamodel.resetModuleManager();
  }

  @Test
  public void testOverride() throws Exception {
    compiler.assertCompile("helloworld");
    compiler.assertCompile("override");
    RunnerOptions options = new RunnerOptions();
    options.addExtraModule("helloworld", "1.0.0");
    JavaRunner runner = compiler.runner(options, "override", "1.0.0");
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
    compiler.assertCompile("noopverticle");
    RunnerOptions options = new RunnerOptions();
    options.addExtraModule("noopverticle", "1.0.0");
    options.addUserRepository(Helper.assertVertxRepo().getAbsolutePath());
    JavaRunner runner = compiler.runner(options, "io.vertx.ceylon.platform", "0.4.0");
    runner.run();
    ClassLoader loader = runner.getModuleClassLoader();
    Method findVerticlesMethod = loader.loadClass("io.vertx.ceylon.platform.findVerticles_").getDeclaredMethod("findVerticles", Set.class);
    List<Callable<Verticle>> factories = (List<Callable<Verticle>>) findVerticlesMethod.invoke(null, Collections.singleton("noopverticle"));
    assertEquals(1, factories.size());
    Verticle verticle = factories.get(0).call();
    assertTrue(Verticle.class.isInstance(verticle));
    runner.cleanup();
    Metamodel.resetModuleManager();
  }
}
