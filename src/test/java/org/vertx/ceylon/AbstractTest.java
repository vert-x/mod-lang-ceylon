package org.vertx.ceylon;

import com.redhat.ceylon.compiler.java.runtime.tools.*;
import org.junit.Before;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class AbstractTest {

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

  protected void assertCompile(String module) {
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
}
