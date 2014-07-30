package org.vertx.ceylon;

import com.redhat.ceylon.compiler.java.runtime.tools.Backend;
import com.redhat.ceylon.compiler.java.runtime.tools.CeylonToolProvider;
import com.redhat.ceylon.compiler.java.runtime.tools.CompilationListener;
import com.redhat.ceylon.compiler.java.runtime.tools.CompilerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.Runner;
import com.redhat.ceylon.compiler.java.runtime.tools.RuntimeOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.Compiler;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class BasicTest {

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

  private void run(String module, String version) {
    File sourcePath = new File("src/test/resources");
    assertTrue(sourcePath.exists());
    assertTrue(sourcePath.isDirectory());

    File modules = new File("target/modules");
    if (!modules.exists()) {
      assertTrue(modules.mkdirs());
    } else {
      assertTrue(modules.isDirectory());
    }

    File systemRepo = new File("target/system-repo");
    assertTrue(systemRepo.isDirectory());
    assertTrue(systemRepo.exists());

    CompilerOptions options = new CompilerOptions();
    options.setSourcePath(Collections.singletonList(sourcePath));
    options.setOutputRepository(modules.getAbsolutePath());
    options.setSystemRepository(systemRepo.getAbsolutePath());
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

    RuntimeOptions runtimeOptions = new RuntimeOptions();
    runtimeOptions.setSystemRepository(systemRepo.getAbsolutePath());
    runtimeOptions.addUserRepository(modules.getAbsolutePath());

    Runner runner = CeylonToolProvider.getRunner(Backend.Java, runtimeOptions, module, version);
    runner.run();
  }

  @Test
  public void testCompile() {
    run("helloworld", "1.0.0");
  }

  @Test
  public void testSDK() {
    run("sdk", "1.0.0");
  }

  @Test
  public void testOverride() {
    run("override", "1.0.0");
  }

}
