package org.vertx.ceylon;

import org.vertx.java.core.logging.impl.LogDelegate;
import org.vertx.java.core.logging.impl.LogDelegateFactory;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class LogDelegateFactoryImpl implements LogDelegateFactory {

  @Override
  public LogDelegate createDelegate(String name) {
    return new LogDelegate() {
      @Override
      public boolean isInfoEnabled() {
        return true;
      }

      @Override
      public boolean isDebugEnabled() {
        return true;
      }

      @Override
      public boolean isTraceEnabled() {
        return true;
      }

      private void log(String level, Object message) {
        System.out.println("[" + level + "] " + message);
      }

      private void log(String level, Object message, Throwable t) {
        System.out.println("[" + level + "] " + message);
        t.printStackTrace();
      }

      @Override
      public void fatal(Object message) {
        log("fatal", message);
      }

      @Override
      public void fatal(Object message, Throwable t) {
        log("fatal", message, t);
      }

      @Override
      public void error(Object message) {
        log("error", message);
      }

      @Override
      public void error(Object message, Throwable t) {
        log("error", message, t);
      }

      @Override
      public void warn(Object message) {
        log("warn", message);
      }

      @Override
      public void warn(Object message, Throwable t) {
        log("warn", message, t);
      }

      @Override
      public void info(Object message) {
        log("info", message);
      }

      @Override
      public void info(Object message, Throwable t) {
        log("info", message, t);
      }

      @Override
      public void debug(Object message) {
        log("debug", message);
      }

      @Override
      public void debug(Object message, Throwable t) {
        log("debug", message, t);
      }

      @Override
      public void trace(Object message) {
        log("trace", message);
      }

      @Override
      public void trace(Object message, Throwable t) {
        log("trace", message, t);
      }
    };
  }
}
