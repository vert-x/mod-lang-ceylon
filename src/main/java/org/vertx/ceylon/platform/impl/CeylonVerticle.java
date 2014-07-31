package org.vertx.ceylon.platform.impl;

import com.redhat.ceylon.compiler.java.runtime.tools.*;
import com.redhat.ceylon.compiler.java.runtime.tools.Compiler;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CeylonVerticle extends Verticle {

  final ClassLoader delegatLoader;
  final File sourcePath;
  final File userRepo;
  final List<File> sources;

  public CeylonVerticle(ClassLoader delegatLoader, File sourcePath, File userRepo, List<File> sources) {
    this.delegatLoader = delegatLoader;
    this.sourcePath = sourcePath;
    this.userRepo = userRepo;
    this.sources = sources;
  }

  @Override
  public void start(Future<Void> startedResult) {
    try {
      JsonObject config = container.config();
      if (config == null) {
        config = new JsonObject();
      }

      //
      String systemRepo = config.getString("systemRepo", null);
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
      compilerOptions.setVerbose(config.getBoolean("verbose", false));

      //
      final JavaRunnerOptions runnerOptions = new JavaRunnerOptions();
      Compiler compiler = CeylonToolProvider.getCompiler(Backend.Java);
      final ArrayList<String> modules = new ArrayList<>();
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
          System.out.println("Compiled module " + s + "/" + s2);
          modules.add(s);
          runnerOptions.addExtraModule(s, s2);
        }
      });
      if (!compiled) {
        throw new Exception("Could not compile");
      }

      //
      runnerOptions.setDelegateClassLoader(delegatLoader);
      runnerOptions.setSystemRepository(systemRepo);
      runnerOptions.addUserRepository(userRepo.getAbsolutePath());
      JavaRunner runner = (JavaRunner) CeylonToolProvider.getRunner(Backend.Java, runnerOptions, "io.vertx.ceylon", "0.4.0");
      runner.run();
      ClassLoader loader = runner.getModuleClassLoader();
      Method introspector = loader.loadClass("io.vertx.ceylon.metamodel.introspector_").getDeclaredMethod("introspector", List.class);
      List<Verticle> verticles = (List<Verticle>) introspector.invoke(null, modules);
      if (verticles.size() == 0) {
        throw new Exception("No verticle found in modules " + modules);
      } else if (verticles.size() > 1) {
        throw new Exception("Too many verticles found " + verticles + " in " + modules);
      }
      Verticle verticle = verticles.get(0);
      verticle.setContainer(container);
      verticle.setVertx(vertx);
      verticle.start();

      // Ok
      startedResult.setResult(null);
    } catch (Exception e) {
      startedResult.setFailure(e);
    }
  }
}
