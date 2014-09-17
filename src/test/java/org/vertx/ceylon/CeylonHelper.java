package org.vertx.ceylon;

import com.redhat.ceylon.compiler.java.runtime.tools.Backend;
import com.redhat.ceylon.compiler.java.runtime.tools.CeylonToolProvider;
import com.redhat.ceylon.compiler.java.runtime.tools.CompilationListener;
import com.redhat.ceylon.compiler.java.runtime.tools.CompilerOptions;
import com.redhat.ceylon.compiler.java.runtime.tools.JavaRunner;
import com.redhat.ceylon.compiler.java.runtime.tools.RunnerOptions;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CeylonHelper {

  File modules;
  File sourcePath;
  File systemRepo;

  public CeylonHelper() {
    sourcePath = new File("src/test/resources");
    assertTrue(sourcePath.exists());
    assertTrue(sourcePath.isDirectory());
    modules = Helper.assertModules();
    systemRepo = Helper.assertSystemRepo();
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

  public void assertCompile(String module) {
    CompilerOptions options = new CompilerOptions();
    options.setSourcePath(Collections.singletonList(sourcePath));
    options.setOutputRepository(modules.getAbsolutePath());
    options.setSystemRepository("flat:" + systemRepo.getAbsolutePath());
    scan(options, new File(sourcePath, module));
    com.redhat.ceylon.compiler.java.runtime.tools.Compiler compiler = CeylonToolProvider.getCompiler(Backend.Java);
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


  public JavaRunner runner(String module, String version) {
    return runner(new RunnerOptions(), module, version);
  }

  public JavaRunner runner(RunnerOptions runnerOptions, String module, String version) {
    runnerOptions.setSystemRepository("flat:" + systemRepo.getAbsolutePath());
    runnerOptions.addUserRepository(modules.getAbsolutePath());
    return (JavaRunner) CeylonToolProvider.getRunner(Backend.Java, runnerOptions, module, version);
  }
}
