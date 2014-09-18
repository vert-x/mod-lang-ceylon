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

## Running a Verticle

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

### a precompiled Verticle

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
- SharedMap and SharedSet : inherit Ceylon stuff again
- test a module deployed in a repository
- config a particular Verticle as a Ceylon module could contain several Verticle
- allow to have module imports for ceylon script deployment
- javascript client lib

## Build instructions

- build a snapshot of Ceylon 1.1: instructions here https://github.com/ceylon/ceylon-dist
- deploy a snapshot of the Vert.x Api for Ceylon in your local Ceylon repository
    - checkout https://github.com/vietj/ceylon-vertx
    - `ant install`
- deploy a snapshot of the Ceylon lang module for Vert.x in your Maven repository
    - mvn install

