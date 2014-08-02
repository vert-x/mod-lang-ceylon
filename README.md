# Ceylon lang module for Vert.x 2.x and Ceylon 1.1

## How to build

- build a snapshot of Ceylon 1.1: instructions here https://github.com/ceylon/ceylon-dist
- deploy a snapshot of the Vert.x Api for Ceylon in your local Ceylon repository
    - checkout https://github.com/vietj/ceylon-vertx
    - `ant install`
- deploy a snapshot of the Ceylon lang module for Vert.x in your Maven repository
    - mvn install

## Installation

Edit the file $VERTX_HOME/conf/langs.properties

- declare the Ceylon Verticle Factory with `ceylon=io.vertx~lang-ceylon~1.0.0-alpha1-SNAPSHOT:org.vertx.ceylon.platform.impl.CeylonVerticleFactory`
- declare the file mapping with `.ceylon=ceylon`

## Running a module

### From sources

This mode compiles and runs a Vert.x module written in Ceylon. You need to be at the root of the module source
and run the Ceylon module file, example

~~~~
cd src/test/resources
vertx run httpserververticle/module.ceylon
~~~~

### A precompiled module

This mode assumes you are running a precompiled module found in the Ceylon repository, the default user repository
is automatically configured. Assuming the default user repository contains the `httpserververticle` module:

~~~~
vertx run ceylon:httpserververticle/1.0.0
~~~~

### Deployment options

When running a module, options can be specified, mod-lang-ceylon defines two configuration options:

- `systemRepo`: provide an explicit system repository and overrides the default system repository contained in mod-lang-ceylon
- `userRepo`: provide an explicit user repository where Ceylon stores compiled modules or look for existing modules, when
such repository does not exist, mod-lang-ceylon will create a temporary repository
- `verbose`: activate Ceylon verbose option, useful for debugging

## Todo

- Allow to resolve maven repositories ?
- run Ceylon files with automatic import of the `io.vertx.ceylon` module (either using a Compiler option or cheating
by creating a module structure and a predefined module.properties)
- Ceylon verticle/module mod zip packaging
