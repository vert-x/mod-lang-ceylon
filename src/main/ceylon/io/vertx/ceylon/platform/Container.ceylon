import org.vertx.java.platform { Container_ = Container }
import org.vertx.java.core.json { JsonObject_=JsonObject }
import ceylon.json { JsonObject=Object }
import io.vertx.ceylon.core.util { toJsonObject, fromJsonObject, AsyncResultPromise }
import java.lang { String_=String }
import ceylon.promise { Promise }
import ceylon.collection { HashMap }
import ceylon.logging { Logger, Priority, Category, trace_=trace, debug_=debug, info_=info, error_=error, warn_=warn, fatal_=fatal }

JsonObject_? toConf(JsonObject? c) {
    if (exists c) {
        return toJsonObject(c);
    } else {
        return null;
    }
}

Deployment fa(Anything(String) undeploy)(String_? a) {
    if (exists a) {
        return Deployment(a.string, undeploy);
    } else {
        throw Exception();
    }
}

"""This class represents a Verticle's view of the container in which it is running.
   An instance of this class will be created by the system and made available to
   a running Verticle.
   It contains methods to programmatically deploy other verticles, undeploy
   verticles, deploy modules, get the configuration for a verticle and get the logger for a
   verticle, amongst other things.
   """
shared class Container(Container_ delegate) {
  
    value loggerDelegate = delegate.logger();
  
    "The verticle logger"
    shared object logger satisfies Logger {

      // Can we do better ?
      shared actual Category category => `module io.vertx.ceylon.platform`;
      
      shared actual void log(Priority priority, Logger.Message message, Exception? exception) {
        String a;
        switch (message) 
        case (is String) {
          a = message;
        }
        case (is String()) {
          a = message();
        }
        switch(priority)
        case (trace_) {
          if (exists exception) {
            loggerDelegate.trace(a, exception);
          } else {
            loggerDelegate.trace(a);
          }
        }
        case (debug_) {
          if (exists exception) {
            loggerDelegate.debug(a, exception);
          } else {
            loggerDelegate.debug(a);
          }
        }
        case (info_) {
          if (exists exception) {
            loggerDelegate.info(a, exception);
          } else {
            loggerDelegate.info(a);
          }
        }
        case (warn_) {
          if (exists exception) {
            loggerDelegate.warn(a, exception);
          } else {
            loggerDelegate.warn(a);
          }
        }
        case (error_) {
          if (exists exception) {
            loggerDelegate.error(a, exception);
          } else {
            loggerDelegate.error(a);
          }
        }
        case (fatal_) {
          if (exists exception) {
            loggerDelegate.fatal(a, exception);
          } else {
            loggerDelegate.fatal(a);
          }
        }
      }
      
      shared actual Priority priority {
        if (loggerDelegate.traceEnabled) {
          return trace_;
        } else if (loggerDelegate.debugEnabled) {
          return debug_;
        } else if (loggerDelegate.infoEnabled) {
          return info_;
        } else {
          return error_;
        }
      }
      
      assign priority {
        // Read only
      }
    }
    
    "Get an unmodifiable map of system, environment variables."
    shared object env satisfies Map<String, String> {
      
      shared actual Map<String,String> clone() => this;
      
      shared actual Boolean defines(Object key) {
        if (is String key) {
          if (delegate.env().containsKey(String_(key))) {
            return true;
          }
        }
        return false;
      }
      
      shared actual String? get(Object key) {
        if (is String key) {
          String_? val = delegate.env().get(String_(key));
          if (exists val) {
            return val.string;
          }
        }
        return null;
      }
      
      shared actual Iterator<String->String> iterator() {
        value it = delegate.env().entrySet().iterator();
        object i satisfies Iterator<String->String> {
          shared actual <String->String>|Finished next() {
            if (it.hasNext()) {
              value next = it.next();
              return next.key.string->next.\ivalue.string;
            } else {
              return finished;
            }
          }
        }
        return i;
      }
      
      shared actual Boolean equals(Object that) => (super of Map<String, String>).equals(that);
      
      shared actual Integer hash => (super of Map<String, String>).hash;
    }
    
    "Get the verticle configuration"
    JsonObject_? config_ = delegate.config();
    shared JsonObject? config;
    if (exists config_) {
        config = fromJsonObject(delegate.config());
    } else {
        config = null;
    }
    
    "Cause the container to exit"
    shared void exit() {
        delegate.exit();
    }

    "Deploy a worker verticle programmatically. The returned promise will be resolved with the deployment or be rejected if it fails to deploy"
    shared Promise<Deployment> deployWorkerVerticle(
        "The main of the verticle"
        String main, 
        "The number of instances to deploy (defaults to 1)"
        Integer instance = 1, 
        "Multithreaded or not"
        Boolean multiThreaded = false, 
        "JSON config to provide to the verticle"
        JsonObject? conf = null) {
        JsonObject_? conf_ = toConf(conf);
        void undeploy(String s) {
            delegate.undeployVerticle(s);
        }
        AsyncResultPromise<Deployment, String_> a = AsyncResultPromise<Deployment, String_>(fa(undeploy));
        delegate.deployWorkerVerticle(main, conf_, instance, multiThreaded, a);
        return a.promise;
    }
    
    "Deploy a module programmatically. The returned promise will be resolved with the deployment or be rejected if it fails to deploy"
    shared Promise<Deployment> deployModule(
        "The main of the module to deploy"
        String moduleName, 
        "The number of instances to deploy (defaults to 1)"
        Integer instance = 1, 
        "JSON config to provide to the module"
        JsonObject? conf = null) {
        JsonObject_? conf_ = toConf(conf);
        void undeploy(String s) {
            delegate.undeployModule(s);
        }
        AsyncResultPromise<Deployment, String_> a = AsyncResultPromise<Deployment, String_>(fa(undeploy));
        delegate.deployModule(moduleName, conf_, instance, a);
        return a.promise;
    }

    "Deploy a verticle programmatically. The returned promise will be resolved with the deployment or be rejected if it fails to deploy"
    shared Promise<Deployment> deployVerticle(
        "The main of the verticle"
        String main, 
        "The number of instances to deploy (defaults to 1)"
        Integer instance = 1, 
        "JSON config to provide to the verticle"
        JsonObject? conf = null) {
        JsonObject_? conf_ = toConf(conf);
        void undeploy(String s) {
            delegate.undeployVerticle(s);
        }
        AsyncResultPromise<Deployment, String_> a = AsyncResultPromise<Deployment, String_>(fa(undeploy));
        delegate.deployVerticle(main, conf_, instance, a);
        return a.promise;
    }
    
}