package org.vertx.ceylon.platform.impl;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;
import org.vertx.java.platform.VerticleFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CeylonVerticleFactory implements VerticleFactory {

  private Vertx vertx;
  private Container container;
  private ClassLoader cl;

  @Override
  public void init(Vertx vertx, Container container, ClassLoader cl) {
    this.cl = cl;
    this.container = container;
    this.cl = cl;
    this.vertx = vertx;
  }

  @Override
  public Verticle createVerticle(String main) throws Exception {

    //
    if (main.endsWith(".ceylon")) {
      File sourcePath = new File(cl.getResource("").toURI());
      File moduleSrc = new File(cl.getResource(main).toURI());
      File userRepo = File.createTempFile("vertx", ".repo");
      userRepo.delete();
      userRepo.mkdir();
      userRepo.deleteOnExit();
      ArrayList<File> sources = new ArrayList<>();
      scan(sources, moduleSrc.getParentFile());
      CeylonVerticle verticle = new CeylonVerticle(cl, sourcePath, userRepo, sources);
      verticle.setVertx(vertx);
      verticle.setContainer(container);
      return verticle;
    }

    throw new UnsupportedOperationException("Implement create verticle " + main);
  }

  @Override
  public void reportException(Logger logger, Throwable t) {
    logger.error("Exception in Ceylon verticle", t);
  }

  @Override
  public void close() {

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
