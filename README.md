# Ceylon lang module for Vert.x 2.1.2 and Ceylon 1.1

## Documentation

- [Official Vert.x documentation](http://vertx.io/docs.html)
- [`io.vertx.ceylon.core` module](http://vertx.io/mod-lang-ceylon/core/)
- [`io.vertx.ceylon.platform` module](http://vertx.io/mod-lang-ceylon/platform/)

## Installation

We suppose `$VERTX_HOME` points to Vert.x 2.1.2. Edit `$VERTX_HOME/conf/langs.properties` lang configuration:

- declare the Ceylon Verticle Factory with `ceylon=io.vertx~lang-ceylon~1.0.0:org.vertx.ceylon.platform.impl.CeylonVerticleFactory`
- declare the file mapping with `.ceylon=ceylon`

Note that the Vert.x lang mod comes with the Ceylon language out of the box. If you want to install Ceylon 1.1
you can easily build it from the [sources](https://github.com/ceylon/ceylon-dist/) as well as
[Ceylon IDE](https://github.com/ceylon/ceylon-ide-eclipse).

## Running a Verticle

### from a Verticle source

Create a new `HelloWorldServer.ceylon`:

~~~~
import io.vertx.ceylon.platform {
  Verticle,
  Container
}
import io.vertx.ceylon.core {
  Vertx
}
import io.vertx.ceylon.core.http {
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
Module io.vertx~lang-ceylon~1.0.0 successfully installed
Create temporary source path /var/folders/79/mjy6k9xs6l74_cf_zjg8klgc0000gn/T/vertx4695595712845187618ceylon for /Users/julien/java/mod-lang-ceylon/src/examples/ceylon/examples/httphelloworld/HelloWorldServer.ceylon
Compiled module app/1.0.0
Succeeded in deploying verticle
~~~~

You can download more [examples](http://search.maven.org/remotecontent?filepath=io/vertx/lang-ceylon/1.0.0/lang-ceylon-1.0.0-examples.zip) or view them
[here](https://github.com/vert-x/mod-lang-ceylon/tree/master/src/examples/ceylon/examples).

### from a Ceylon module containing a Verticle

This mode compiles and runs a Vert.x module written in Ceylon:

From the previous example, create a directory `httpserververticle` and move the `HelloWorldServer.ceylon`
in this folder.

Add a module descriptor `module.ceylon`:

~~~~
module httpserververticle "1.0.0" {
  shared import "io.vertx.ceylon" "1.0.0";
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

### A precompiled Verticle

In the previous examples, Vert.x was compiling the module for you, `mod-lang-ceylon` can also run compiled modules
from your Ceylon repository. This means you have compiled this module with the `ceylon compile` command or Ceylon IDE
and installed it in your user repository `$HOME/.ceylon`:

~~~~
vertx run ceylon:httpserververticle/1.0.0
Succeeded in deploying verticle
~~~~

### Specifying the main Verticle

A failure will occur when a module contains several verticle instances, because there is an ambiguity about the
 Verticle to use. A Verticle can deploy other Verticles, so it is valid to have several Verticle, in such situation
 the `io.vertx.ceylon.platform.main` annotation can be used to specify the _main_ verticle of this module:

~~~~
import io.vertx.ceylon.platform { main }

main(`class MainVerticle`)
module myapp "1.0.0" {
  shared import "io.vertx.ceylon.platform" "1.0.0";
}
~~~~

### Deployment options

When running a module, options can be specified, mod-lang-ceylon defines two configuration options:

- `systemRepo`: provide an explicit system repository and overrides the default system repository contained in mod-lang-ceylon
- `userRepo`: provide an explicit user repository where Ceylon stores compiled modules or look for existing modules, when
such repository does not exist, mod-lang-ceylon will create a temporary repository
- `verbose`: activate Ceylon verbose option, useful for debugging
- `mainVerticle`: specify a Verticle to deploy, usually used when you deploy a module that could contain several verticles. This
will override any `main` annotation

## Creating a module

A module is a Ceylon verticle packaged in a zip file with a `mod.json` descriptor at the root of the zip. Usually
a module packages the various libraries required by the module in a `lib` directory. Ceylon has its own module system
and works best with it, therefore for Ceylon only the `mod.json` file is required and its `main` entry should contain the
name of the Ceylon module prefixed by `ceylon`, like the precompiled Verticle seen before.

~~~~
{
   "main": "ceylon:httpserververticle/1.0.0"
}
~~~~

~~~~
% vertx runmod my~httpserververticle~1.0.0
~~~~

This module will be resolved in the default module repository of the platform. The `userRepo` configuration can be
 used for resolving the module from this location instead, note this is specified at run time:

~~~~
% echo '{ "userRepo":"/modules" }' > conf.json
% vertx runmod my~httpserververticle~1.0.0 -conf conf.json
~~~~

## Todo

- Allow to resolve maven repositories ?
- test a module deployed in a repository
- allow to have module imports for ceylon script deployment
- javascript client lib

## Build instructions

- build a snapshot of Ceylon 1.1: instructions here https://github.com/ceylon/ceylon-dist
- deploy a snapshot of the Vert.x Api for Ceylon in your local Ceylon repository
    - checkout [the project](https://github.com/vietj/ceylon-vertx)
    - `ant install`
- deploy a snapshot of the Ceylon lang module for Vert.x in your Maven repository
    - `mvn install`

