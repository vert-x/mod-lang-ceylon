package org.vertx.ceylon.platform.impl;

import com.redhat.ceylon.compiler.java.runtime.metamodel.Metamodel;
import com.redhat.ceylon.compiler.java.runtime.tools.*;
import com.redhat.ceylon.compiler.java.runtime.tools.Compiler;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CeylonVerticle extends Verticle {

  final String main;
  final ClassLoader delegateLoader;
  private Verticle verticle;
  private JavaRunner runner;

  public CeylonVerticle(String main, ClassLoader delegateLoader) {
    this.main = main;
    this.delegateLoader = delegateLoader;
    this.verticle = null;
  }

  @Override
  public void start(Future<Void> startedResult) {
    try {
      JsonObject config = container.config();
      if (config == null) {
        config = new JsonObject();
      }

      // Verbose
      boolean verbose = config.getBoolean("verbose", false);

      // User repo
      String userRepo = config.getString("userRepo", null);
      if (userRepo == null) {
        File tmpUserRepo = File.createTempFile("vertx", ".repo");
        tmpUserRepo.delete();
        tmpUserRepo.mkdir();
        tmpUserRepo.deleteOnExit();
        userRepo = tmpUserRepo.getAbsolutePath();
      }

      // Use provided system repo / auto guess system repo
      String systemRepo = config.getString("systemRepo", null);
      String systemPrefix = "";
      if (systemRepo == null) {
        URL systemRepoURL = Compiler.class.getClassLoader().getResource("lib/");
        if (systemRepoURL == null || !systemRepoURL.getProtocol().equals("file")) {
          throw new Exception("Can't rutn without system repo");
        }
        systemRepo = new File(systemRepoURL.toURI()).getAbsolutePath();
        systemPrefix = "flat:";
      }

      // Vert.x repo in packaged mod contains SDK + io.vertx.ceylon
      URL vertxRepoURL = Compiler.class.getClassLoader().getResource("repo/");
      String vertxRepo;
      if (vertxRepoURL != null && vertxRepoURL.getProtocol().equals("file")) {
        vertxRepo = new File(vertxRepoURL.toURI()).getAbsolutePath();
        container.logger().debug("Using vertxRepo " + vertxRepo);
      } else {
        vertxRepo = null;
        container.logger().debug("No vertxRepo found");
      }

      //
      final JavaRunnerOptions runnerOptions = new JavaRunnerOptions();
      final HashSet<String> modules = new HashSet<>();
      if (main.endsWith(".ceylon")) {

        URL mainResource = delegateLoader.getResource(main);
        if (mainResource == null) {
          throw new Exception("Cannot resolve " + main);
        }
        if (!mainResource.getProtocol().equals("file")) {
          throw new Exception("Unsupported main url " + mainResource);
        }
        File moduleSrc = new File(mainResource.toURI());
        if (!moduleSrc.isFile()) {
          throw new Exception("Main " + main + " is not a file");
        }

        File sourcePath;
        if (moduleSrc.getName().equals("module.ceylon")) {
          sourcePath = new File(delegateLoader.getResource("").toURI());
        } else {
          sourcePath = File.createTempFile("vertx", "ceylon");
          if (!sourcePath.delete() || !sourcePath.mkdir()) {
            throw new Exception("Could not create temp dir " + sourcePath.getCanonicalPath());
          }
          container.logger().info("Create temporary source path " + sourcePath.getAbsolutePath() + " for " +
              moduleSrc.getAbsolutePath());
          sourcePath.deleteOnExit();
          File moduleDir = new File(sourcePath, "app");
          moduleDir.mkdir();
          Files.copy(moduleSrc.toPath(), new File(moduleDir, moduleSrc.getName()).toPath());
          moduleSrc = new File(moduleDir, "module.ceylon");
          moduleSrc.createNewFile();
          Files.write(moduleSrc.toPath(), (
              "module app \"1.0.0\" {\n" +
              "shared import io.vertx.ceylon \"0.4.0\";\n" +
              "}\n"
          ).getBytes());
          File packageSrc = new File(moduleDir, "package.ceylon");
          packageSrc.createNewFile();
          Files.write(packageSrc.toPath(), (
              "shared package app;\n"
          ).getBytes());
        }

        ArrayList<File> sources = new ArrayList<>();
        scan(sources, moduleSrc.getParentFile());

        // java.class.path work around hack
        StringBuffer javaClassPath = new StringBuffer();
        String previous = System.getProperty("java.class.path");
        if (previous != null) {
          javaClassPath.append(previous);
        }
        File[] children = new File(systemRepo).listFiles();
        if (children != null) {
          for (File child : children) {
            if (javaClassPath.length() > 0) {
              javaClassPath.append(':');
            }
            javaClassPath.append(child.getAbsolutePath());
          }
        }
        System.setProperty("java.class.path", "" + javaClassPath);

        //
        CompilerOptions compilerOptions = new CompilerOptions();
        compilerOptions.setSourcePath(Collections.singletonList(sourcePath));
        compilerOptions.setFiles(sources);
        compilerOptions.setOutputRepository(userRepo);
        if (vertxRepo != null) {
          compilerOptions.addUserRepository(vertxRepo);
        }
        compilerOptions.setSystemRepository(systemPrefix + systemRepo);
        compilerOptions.setVerbose(verbose);

        //
        Compiler compiler = CeylonToolProvider.getCompiler(Backend.Java);
        boolean compiled = compiler.compile(compilerOptions, new CompilationListener() {
          @Override
          public void error(File file, long line, long column, String message) {
            Logger logger = container.logger();
            String msg;
            if (file != null) {
              msg = "Compilation error at (" + line + "," + column + ") in " +
                  file.getAbsolutePath() + ":" + message;
            } else {
              msg = "Compilation error:" + message;
            }
            logger.error(msg);
          }
          @Override
          public void warning(File file, long line, long column, String message) {
            String msg;
            if (file != null) {
              msg = "Compilation warning at (" + line + "," + column + ") in " +
                  file.getAbsolutePath() + ":" + message;
            } else {
              msg = "Compilation warning:" + message;
            }
            container.logger().warn(msg);
          }
          @Override
          public void moduleCompiled(String module, String version) {
            container.logger().info("Compiled module " + module + "/" + version);
            modules.add(module);
            runnerOptions.addExtraModule(module, version);
          }
        });
        System.setProperty("java.class.path", previous); // Restore
        if (!compiled) {
          throw new Exception("Could not compile");
        }
      } else {
        Pattern pattern = Pattern.compile("([\\p{Alpha}.]+)/([\\p{Alnum}.]+)");
        Matcher matcher = pattern.matcher(main);
        if (matcher.matches()) {
          String module = matcher.group(1);
          String version = matcher.group(2);
          runnerOptions.addExtraModule(module, version);
          modules.add(module);
        } else {
          throw new Exception("Invalid module " + main + " should be module/version");
        }
      }

      //
      runnerOptions.setDelegateClassLoader(CeylonVerticle.class.getClassLoader());
      runnerOptions.setSystemRepository(systemPrefix + systemRepo);
      runnerOptions.addUserRepository(userRepo);
      if (vertxRepo != null) {
        runnerOptions.addUserRepository(vertxRepo);
      }
      runnerOptions.setVerbose(verbose);
      runner = (JavaRunner) CeylonToolProvider.getRunner(Backend.Java, runnerOptions, "io.vertx.ceylon", "0.4.0");
      runner.run();
      ClassLoader loader = runner.getModuleClassLoader();
      Method introspector = loader.loadClass("io.vertx.ceylon.metamodel.findVerticles_").getDeclaredMethod("findVerticles", Set.class);
      List<Callable<Verticle>> factories = (List<Callable<Verticle>>) introspector.invoke(null, modules);
      if (factories.size() == 0) {
        throw new Exception("No verticle found in modules " + modules);
      } else if (factories.size() > 1) {
        throw new Exception("Too many verticles found " + factories + " in " + modules);
      }
      verticle = factories.get(0).call();
      verticle.setContainer(container);
      verticle.setVertx(vertx);
      verticle.start();

      // Ok
      startedResult.setResult(null);
    } catch (Exception e) {
      startedResult.setFailure(e);
    }
  }

  @Override
  public void stop() {
    if (verticle != null) {
      verticle.stop();
    }
    if (runner != null) {
      runner.cleanup();
    }
    Metamodel.resetModuleManager();
  }

  private void scan(List<File> sources, File file) {
    if (file.exists()) {
      if (file.isDirectory()) {
        File[] children = file.listFiles();
        if (children != null) {
          for (File child : children) {
            scan(sources, child);
          }
        }
      } else if (file.isFile() && file.getName().endsWith(".ceylon")) {
        sources.add(file);
      }
    }
  }
}
