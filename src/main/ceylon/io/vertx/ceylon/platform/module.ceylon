"""# Writing Verticles
   
   As was described in the [main manual](http://vertx.io/manual.html#verticle), a verticle is the execution unit of Vert.x.
   
   To recap, Vert.x is a container which executes packages of code called Verticles, and it ensures that the code in the
   verticle is never executed concurrently by more than one thread. You can write your verticles in any of the languages
   that Vert.x supports, and Vert.x supports running many verticle instances concurrently in the same Vert.x instance.
   
   All the code you write in a Vert.x application runs inside a Verticle instance.
   
   For simple prototyping and trivial tasks you can write raw verticles and run them directly on the command line,
   but in most cases you will always wrap your verticles inside Vert.x modules.
   
   For now, let's try writing a simple raw verticle.
   
   As an example we'll write a simple TCP echo server. The server just accepts connections and any data received
   by it is echoed back on the connection.
   
   Copy the following into a text editor and save it as `Server.ceylon` 
   
   ~~~
   import io.vertx.ceylon.platform { Verticle, Container }
   import io.vertx.ceylon.core { Vertx }
   import io.vertx.ceylon.core.net { NetSocket }
   import io.vertx.ceylon.core.stream { Pump }
   
   shared class Server() extends Verticle() {
     shared void start(Vertx vertx, Container container) {
       vertx.createNetServer().connectHandler(void (NetSocket sock) {
         Pump(sock.readStream, sock.writeStream).start();
       }).listen(1234);
     }
   }
   ~~~
   
   Now run it:
   
   ~~~
   vertx run Server.ceylon
   ~~~
   
   The server will now be running. Connect to it using telnet:
   
   ~~~
   telnet localhost 1234
   ~~~
   
   And notice how data you send (and hit enter) is echoed back to you.
   
   Congratulations! You've written your first verticle.
   
   Notice how you didn't have to first compile the `.ceylon` file to a module. Vert.x understands how to run .ceylon files
   directly - internally doing the compilation on the fly. (It also supports running modulesw too if you prefer)
   
   Every Ceylon verticle must extend the class [[Verticle]]. You must override the start method - this is called by Vert.x
   when the verticle is started.
   
   _In the rest of this manual we'll assume the code snippets are running inside a verticle._
   
   ## Asynchronous start
  
   In some cases your Verticle has to do some other stuff asynchronously in its [[Verticle.start]] method, e.g. start
   other verticles, and the verticle shouldn't be considered started until those other actions are complete.
   
   If this is the case for your verticle you can implement the asynchronous version [[Verticle.asyncStart]] method:
   
   ~~~
   shared actual Promise<Anything> asyncStart(Vertx vertx, Container container) {
     return container.deployVerticle("foo.js");
   }
   ~~~
   
   ## Verticle clean-up
   
   Servers, clients, event bus handlers and timers will be automatically closed / cancelled when the verticle is
   stopped. However, if you have any other clean-up logic that you want to execute when the verticle is stopped,
   you can implement a [[Verticle.stop]] method which will be called when the verticle is undeployed.

   ## The `container` object
   
   When the verticle starts it gets a [[Container]] instance. This represents the Verticle's view of the container in which it is running.
   
   The container object contains methods for deploying and undeploying verticle and modules, and also allows config,
   environment variables and a logger to be accessed.
   
   ## The `vertx` object
   
   When the verticle starts it gets a the [[io.vertx.ceylon.core::Vertx]] object. This provides access to the Vert.x core API.
   You'll use the Core API to do most things in Vert.x including TCP, HTTP, file system access, event bus, timers etc.
   
   ## Getting Configuration in a Verticle
   
   You can pass configuration to a module or verticle from the command line using the `-conf option, for example:
   
   ~~~
   vertx runmod com.mycompany~my-mod~1.0 -conf myconf.json
   ~~~

   or for a raw verticle
   
   ~~~
   vertx run foo.js -conf myconf.json
   ~~~
   
   The argument to `-conf` is the name of a text file containing a valid JSON object.
   
   That configuration is available inside your verticle by calling the [[Container.config]] method on the container member variable
   of the verticle:
         
   ~~~
   value config = container.config;
   
   print("Config is ``config``");
   ~~~
   
   The config returned is an instance of [[ceylon.json::Object]], which is a class which represents JSON
   objects (unsurprisingly!). You can use this object to configure the verticle.
  
   Allowing verticles to be configured in a consistent way like this allows configuration to be easily passed
   to them irrespective of the language that deploys the verticle.
  
   ## Logging from a Verticle
   
   

   
   """

by("Julien Viet")
license("ASL2")
module io.vertx.ceylon.platform "0.4.0" {

  shared import io.vertx.ceylon.core "0.4.0";

} 
