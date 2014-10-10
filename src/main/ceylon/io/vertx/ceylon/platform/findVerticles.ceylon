import ceylon.language.meta {
  modules
}
import ceylon.language.meta.declaration {
  Module,
  ClassDeclaration,
  OpenType,
  OpenClassType
}
import io.vertx.ceylon.core {
  Vertx
}
import java.lang {
  String_=String,
  Void_=Void
}
import org.vertx.java.platform {
  Verticle_=Verticle
}
import org.vertx.java.core {
  Future_=Future
}
import java.util {
  Map_=Map,
  LinkedHashMap_=LinkedHashMap
}
import java.util.concurrent {
  Callable_=Callable
}
import ceylon.promise {
  Promise
}

OpenType verticleDecl = `class Verticle`.openType;
Boolean isVerticle(OpenType classDecl) {
  if (is OpenClassType classDecl) {
    if (exists ext = classDecl.extendedType) {
      if (ext == verticleDecl) {
        return true;
      } else {
        return isVerticle(ext);
      }
    }
  }
  return false;
}

"Find the verticles for the specified module and return a list of verticle factories. This method is called
 by the Vert.x module to discover the existing Verticles and is somewhat reserved for internal use."
shared Map_<String_,Callable_<Verticle_>> findVerticles(
  "The name of the module to introspect"
  String_ moduleName,
  "The optional verticle name to return"
  String_? verticleName) {
  value verticles = LinkedHashMap_<String_,Callable_<Verticle_>>();
  value mods = modules.list.filter((Module elem) => moduleName == String_(elem.name));
  for (mod in mods) {
    value mainAnnotations = mod.annotations<MainAnnotation>();
    if (exists first = mainAnnotations.first, !verticleName exists) {
      value verticle = foo(first.verticle);
      if (exists verticle) {
        verticles.put(String_(first.verticle.qualifiedName), verticle);
      }
    } else {
      for (pkg in mod.members) {
        {ClassDeclaration*} classDecls;
        if (exists verticleName) {
          classDecls = pkg.members<ClassDeclaration>().filter((ClassDeclaration classDecl) => classDecl.qualifiedName == verticleName.string);
        } else {
          classDecls = pkg.members<ClassDeclaration>();
        }
        for (classDecl in classDecls) {
          value verticle = foo(classDecl);
          if (exists verticle) {
            verticles.put(String_(classDecl.qualifiedName), verticle);
          }
        }
      }
    }
  }
  return verticles;
}

Callable_<Verticle_>? foo(ClassDeclaration classDecl) {
  if (isVerticle(classDecl.openType)) {
    value instance = classDecl.instantiate();
    assert (is Verticle instance);
    object factory satisfies Callable_<Verticle_> {
      shared actual Verticle_ call() {
        object adapter extends Verticle_() {
          shared actual void start(Future_<Void_> future) {
            Vertx vertx = Vertx(this.vertx);
            Container container = Container(this.container);
            Promise<Anything> result = instance.asyncStart(vertx, container);
            result.onComplete(
              void(Anything a) {
                future.setResult(null);
              },
              void(Throwable reason) {
                future.setFailure(reason);
              }
            );
          }
          shared actual void stop() {
            instance.stop();
          }
        }
        return adapter;
      }
    }
    return factory;
  } else {
    return null;
  }
}
