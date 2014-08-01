package org.vertx.ceylon;

import com.redhat.ceylon.compiler.java.runtime.metamodel.Metamodel;
import com.redhat.ceylon.compiler.java.runtime.tools.Backend;
import com.redhat.ceylon.compiler.java.runtime.tools.CeylonToolProvider;
import com.redhat.ceylon.compiler.java.runtime.tools.CompilationListener;
import com.redhat.ceylon.compiler.java.runtime.tools.CompilerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunner;
import com.redhat.ceylon.compiler.java.runtime.tools.RunnerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.Compiler;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Verticle;

import java.io.File;
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


  private JavaRunner runner(String module, String version) {
    return runner(new RunnerOptions(), module, version);
  }

  private JavaRunner runner(RunnerOptions runnerOptions, String module, String version) {
    runnerOptions.setSystemRepository("flat:" + systemRepo.getAbsolutePath());
    runnerOptions.addUserRepository(modules.getAbsolutePath());
    return (JavaRunner) CeylonToolProvider.getRunner(Backend.Java, runnerOptions, module, version);
  }

  @Test
  public void testCompile() {
    assertCompile("helloworld");
    JavaRunner runner = runner("helloworld", "1.0.0");
    runner.run();
    runner.cleanup();
    Metamodel.resetModuleManager();
  }

  @Test
  public void testSDK() {
    assertCompile("sdk");
    JavaRunner runner = runner("sdk", "1.0.0");
    runner.run();
    runner.cleanup();
    Metamodel.resetModuleManager();
  }

  @Test
  public void testOverride() throws Exception {
    assertCompile("helloworld");
    assertCompile("override");
    RunnerOptions options = new RunnerOptions();
    options.addExtraModule("helloworld", "1.0.0");
    JavaRunner runner = runner(options, "override", "1.0.0");
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
    assertCompile("noopverticle");
    RunnerOptions options = new RunnerOptions();
    options.addExtraModule("noopverticle", "1.0.0");
    JavaRunner runner = runner(options, "io.vertx.ceylon", "0.4.0");
    runner.run();
    ClassLoader loader = runner.getModuleClassLoader();
    Method findVerticlesMethod = loader.loadClass("io.vertx.ceylon.metamodel.findVerticles_").getDeclaredMethod("findVerticles", Set.class);
    List<Callable<Verticle>> factories = (List<Callable<Verticle>>) findVerticlesMethod.invoke(null, Collections.singleton("noopverticle"));
    assertEquals(1, factories.size());
    Verticle verticle = factories.get(0).call();
    assertTrue(Verticle.class.isInstance(verticle));
    runner.cleanup();
    Metamodel.resetModuleManager();
  }
}
