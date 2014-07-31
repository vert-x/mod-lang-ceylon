package org.vertx.ceylon.platform.impl;

import com.redhat.ceylon.compiler.java.runtime.tools.*;
import com.redhat.ceylon.compiler.java.runtime.tools.Compiler;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CeylonVerticle extends Verticle {

  final File sourcePath;
  final File userRepo;
  final List<File> sources;

  public CeylonVerticle(File sourcePath, File userRepo, List<File> sources) {
    this.sourcePath = sourcePath;
    this.userRepo = userRepo;
    this.sources = sources;
  }

  @Override
  public void start(Future<Void> startedResult) {
    try {

      String systemRepo = null;

      JsonObject config = container.config();
      if (config != null) {
        systemRepo = config.getString("systemRepo");
      }

      //
      if (systemRepo == null) {
        // Auto guess system repo
        URL jarURL = Compiler.class.getProtectionDomain().getCodeSource().getLocation();
        File jarFile = new File(jarURL.toURI());
        systemRepo = jarFile.getParentFile().getAbsolutePath();
      }

      //
      CompilerOptions compilerOptions = new CompilerOptions();
      compilerOptions.setSourcePath(Collections.singletonList(sourcePath));
      compilerOptions.setOutputRepository(userRepo.getCanonicalPath());
      compilerOptions.setFiles(sources);
      compilerOptions.setSystemRepository(systemRepo);

      //
      Compiler compiler = CeylonToolProvider.getCompiler(Backend.Java);
      final ArrayList<String[]> modules = new ArrayList<>();
      boolean compiled = compiler.compile(compilerOptions, new CompilationListener() {
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
          modules.add(new String[]{s, s2});
          System.out.println("Compiled module " + s + "/" + s2);
        }
      });
      if (!compiled) {
        throw new Exception("Could not compile");
      }

      //
      RunnerOptions runnerOptions = new RunnerOptions();
      runnerOptions.setSystemRepository(systemRepo);
      runnerOptions.addUserRepository(userRepo.getAbsolutePath());
      Runner runner = CeylonToolProvider.getRunner(Backend.Java, runnerOptions, modules.get(0)[0], modules.get(0)[1]);
      runner.run();

      // Ok
      startedResult.setResult(null);
    } catch (Exception e) {
      startedResult.setFailure(e);
    }
  }
}
