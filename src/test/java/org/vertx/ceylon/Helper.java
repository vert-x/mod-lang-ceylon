package org.vertx.ceylon;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Helper {

  public static File assertModules() {
    File modules = new File("target/modules");
    if (!modules.exists()) {
      assertTrue(modules.mkdirs());
    } else {
      assertTrue(modules.isDirectory());
    }
    return modules;
  }

  public static File assertSystemRepo() {
    File systemRepo = new File("target/system-repo");
    assertTrue(systemRepo.isDirectory());
    assertTrue(systemRepo.exists());
    return systemRepo;
  }

  public static File assertVertxRepo() {
    File systemRepo = new File("target/vertx-repo");
    assertTrue(systemRepo.isDirectory());
    assertTrue(systemRepo.exists());
    return systemRepo;
  }
}
