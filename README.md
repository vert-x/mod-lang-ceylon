# Ceylon lang module for Vert.x 2.1.2 and Ceylon 1.1

## Documentation & examples

- [Official Vert.x documentation](http://vertx.io/docs.html)
- [`io.vertx.ceylon.core` module](https://modules.ceylon-lang.org/modules/io.vertx.ceylon.core/1.0.0/doc)
- [`io.vertx.ceylon.platform` module](https://modules.ceylon-lang.org/modules/io.vertx.ceylon.platform/1.0.0/doc)
- [Examples](http://search.maven.org/remotecontent?filepath=io/vertx/lang-ceylon/1.0.1/lang-ceylon-1.0.1-examples.zip) or view them
[here](https://github.com/vert-x/mod-lang-ceylon/tree/master/src/examples/ceylon/examples).
- [Module webapp](http://search.maven.org/remotecontent?filepath=io/vertx/lang-ceylon/1.0.1/lang-ceylon-1.0.1-webapp.zip) or view them
[here](https://github.com/vert-x/mod-lang-ceylon/tree/master/src/examples/ceylon/modules).

## Usage

[Read how to install and use this module](Instructions.md)

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

