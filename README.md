# Ceylon lang module for Vert.x 2.1.2 and Ceylon 1.1

## Documentation

- [Official Vert.x documentation](http://vertx.io/docs.html)
- [Ceylon-vertx module](http://www.julienviet.com/mod-lang-ceylon/module-doc/)

## Installation

We suppose `$VERTX_HOME` points to Vert.x 2.1.2. Edit `$VERTX_HOME/conf/langs.properties` lang configuration:

- declare the Ceylon Verticle Factory with `ceylon=vietj~lang-ceylon~1.0.0-alpha2:org.vertx.ceylon.platform.impl.CeylonVerticleFactory`
- declare the file mapping with `.ceylon=ceylon`

Note that the Vert.x lang mod comes with the Ceylon language out of the box. If you want to install Ceylon 1.1
you can easily build it from the [sources](https://github.com/ceylon/ceylon-dist/) as well as
[Ceylon IDE](https://github.com/ceylon/ceylon-ide-eclipse).

## Running a module

### from a Verticle source

Create a new `HelloWorldServer.ceylon`:

~~~~
import io.vertx.ceylon.platform {
  Verticle,
  Container
}
import io.vertx.ceylon {
  Vertx
}
import io.vertx.ceylon.http {
  HttpServerRequest
}
shared class HelloWorldServer() extends Verticle() {

  shared actual void start(Vertx vertx, Container container) {
    vertx.createHttpServer().requestHandler(void (HttpServerRequest req) {
      req.response.headers { "Content-Type" -> "text/plain" };
      req.response.end("Hello World");
    }).listen(8080);
  }
}
~~~~

Run it with the `vertx` command:

~~~~
% vertx run HelloWorldServer.ceylon
Downloading vietj~lang-ceylon~1.0.0-alpha2. Please wait...
Downloading 100%
Module vietj~lang-ceylon~1.0.0-alpha2 successfully installed
Create temporary source path /var/folders/87/1zztskkd20s8hqvkbn5dy0l00000gn/T/vertx4242384732878054860ceylon for /Users/julien/HelloWorldServer.ceylon
Compiled module app/1.0.0
Succeeded in deploying verticle
~~~~

You can download more [examples](http://www.julienviet.com/mod-lang-ceylon/lang-ceylon-examples.zip) or view them
[here](https://github.com/vietj/mod-lang-ceylon/tree/master/src/test/resources/examples).


### from a Ceylon module containing a Verticle

This mode compiles and runs a Vert.x module written in Ceylon:

From the previous example, create a directory `httpserververticle` and move the `HelloWorldServer.ceylon`
in this folder.

Add a module descriptor `module.ceylon`:

~~~~
module httpserververticle "1.0.0" {
  shared import "io.vertx.ceylon" "0.4.0";
}
~~~~

Add package descriptor `package.ceylon`::

~~~~
shared package httpserververticle;
~~~~

Now at the root of the module source we run the Ceylon module file:

~~~~
% vertx run httpserververticle/module.ceylon
Compiled module httpserververticle/1.0.0
Succeeded in deploying verticle
~~~~

### a precompiled module

In the previous examples, Vert.x was compiling the module for you, `mod-lang-ceylon` can also run compiled modules
from your Ceylon repository. This means you have compiled this module with the `ceylon compile` command or Ceylon IDE
and installed it in your user repository `$HOME/.ceylon`:

~~~~
vertx run ceylon:httpserververticle/1.0.0
Succeeded in deploying verticle
~~~~

### Deployment options

When running a module, options can be specified, mod-lang-ceylon defines two configuration options:

- `systemRepo`: provide an explicit system repository and overrides the default system repository contained in mod-lang-ceylon
- `userRepo`: provide an explicit user repository where Ceylon stores compiled modules or look for existing modules, when
such repository does not exist, mod-lang-ceylon will create a temporary repository
- `verbose`: activate Ceylon verbose option, useful for debugging

## Todo

- Allow to resolve maven repositories ?
- Ceylon verticle/module mod zip packaging
- Increase Ceylon-vertx API coverage

## Build instructions

- build a snapshot of Ceylon 1.1: instructions here https://github.com/ceylon/ceylon-dist
- deploy a snapshot of the Vert.x Api for Ceylon in your local Ceylon repository
    - checkout https://github.com/vietj/ceylon-vertx
    - `ant install`
- deploy a snapshot of the Ceylon lang module for Vert.x in your Maven repository
    - mvn install

