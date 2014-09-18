import ceylon.language.meta { modules }
import ceylon.language.meta.declaration { Module, ClassDeclaration, OpenType, OpenClassType }
import io.vertx.ceylon.core { Vertx }
import java.lang { String_=String, Void_=Void }
import org.vertx.java.platform { Verticle_ = Verticle }
import org.vertx.java.core { Future_=Future }
import java.util { ArrayList_=ArrayList, List_=List, Set_=Set }
import java.util.concurrent { Callable_=Callable }
import ceylon.promise { Promise }

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

"Find the verticles among the known modules and return a list of verticle factories"
shared List_<Callable_<Verticle_>> findVerticles("The set of module names" Set_<String_> moduleNames) {
	value verticles = ArrayList_<Callable_<Verticle_>>();
	value mods = modules.list.filter((Module elem) => moduleNames.contains(String_(elem.name)));
	for (mod in mods) {
		for (pkg in mod.members) {
			for (classDecl in pkg.members<ClassDeclaration>()) {
				if (isVerticle(classDecl.openType)) {
					value instance = classDecl.instantiate();
					assert(is Verticle instance);
					object factory satisfies Callable_<Verticle_> {
					  shared actual Verticle_ call() {
					    object adapter extends Verticle_() {
					      shared actual void start(Future_<Void_> future) {
					        Vertx vertx = Vertx(this.vertx);
					        Container container = Container(this.container);
					        Promise<Anything> result = instance.asyncStart(vertx, container);
					        result.onComplete(
					          void (Anything a) {
					            future.setResult(null);
					          },
					          void (Throwable reason) {
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
					verticles.add(factory);
				}
			}
		}
	}
	return verticles;
}