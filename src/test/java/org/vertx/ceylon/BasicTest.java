package org.vertx.ceylon;

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

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class BasicTest {

  File modules;
  File sourcePath;
  File systemRepo;

  @Before
  public void before() {
    sourcePath = new File("src/test/resources");
    assertTrue(sourcePath.exists());
    assertTrue(sourcePath.isDirectory());

    modules = new File("target/modules");
    if (!modules.exists()) {
      assertTrue(modules.mkdirs());
    } else {
      assertTrue(modules.isDirectory());
    }

    systemRepo = new File("target/system-repo");
    assertTrue(systemRepo.isDirectory());
    assertTrue(systemRepo.exists());
  }

  private void scan(CompilerOptions options, File file) {
    if (file.exists()) {
      if (file.isDirectory()) {
        File[] children = file.listFiles();
        if (children != null) {
          for (File child : children) {
            scan(options, child);
          }
        }
      } else if (file.isFile() && file.getName().endsWith(".ceylon")) {
        options.addFile(file);
      }
    }
  }

  private void assertCompile(String module) {

    CompilerOptions options = new CompilerOptions();
    options.setSourcePath(Collections.singletonList(sourcePath));
    options.setOutputRepository(modules.getAbsolutePath());
    options.setSystemRepository("flat:" + systemRepo.getAbsolutePath());
//    options.setVerbose(true);

    scan(options, new File(sourcePath, module));

    Compiler compiler = CeylonToolProvider.getCompiler(Backend.Java);
    boolean compiled = compiler.compile(options, new CompilationListener() {
      @Override
      public void error(File file, long l, long l2, String s) {
        System.out.println("Error " + s);
      }

      @Override
      public void warning(File file, long l, long l2, String s) {
        System.out.println("Warning " + s);
      }

      @Override
      public void moduleCompiled(String s, String s2) {
        System.out.println("Compiled " + s + " " + s2);
      }
    });

    assertTrue(compiled);
  }

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
    runner("helloworld", "1.0.0").run();
  }

  @Test
  public void testSDK() {
    assertCompile("sdk");
    runner("sdk", "1.0.0").run();
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
  }

  @Test
  public void testVerticleDiscovery() throws Exception {
    assertCompile("noopverticle");
    RunnerOptions options = new RunnerOptions();
    options.addExtraModule("noopverticle", "1.0.0");
    JavaRunner runner = runner(options, "io.vertx.ceylon", "0.4.0");
    runner.run();
    ClassLoader loader = runner.getModuleClassLoader();
    Method introspector = loader.loadClass("io.vertx.ceylon.metamodel.introspector_").getDeclaredMethod("introspector", List.class);
    List<Verticle> verticles = (List<Verticle>) introspector.invoke(null, Collections.singletonList("noopverticle"));
    assertEquals(1, verticles.size());
    assertTrue(verticles.get(0) instanceof Verticle);
  }

}
