package org.vertx.ceylon;

import com.redhat.ceylon.compiler.java.runtime.metamodel.Metamodel;
import org.junit.Before;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class AbstractTest {

  @Before
  public void setUp() {
    Metamodel.resetModuleManager();
  }

}
