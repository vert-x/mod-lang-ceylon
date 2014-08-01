# Ceylon lang module for Vert.x 2.x and Ceylon 1.1

## How to build

- Build a snapshot of Ceylon 1.1: instructions here https://github.com/ceylon/ceylon-dist
- Deploy a snapshot of the Vert.x Api for Ceylon in your local Ceylon repository
    - checkout https://github.com/vietj/ceylon-vertx
    - `ant install`
- Deploy a snapshot of the Ceylon lang module for Vert.x in your Maven repository
    - mvn install


## Usage

The Ceylon lang module will compile the a Vert.x module, you need to be at the root of the module
and run the Ceylon module file, example

~~~~
cd src/test/resources
vertx run httpserververticle/module.ceylon
~~~~