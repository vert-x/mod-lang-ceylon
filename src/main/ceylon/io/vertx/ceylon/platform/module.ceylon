import io.vertx.ceylon.core.net {
  NetSocket
}
import io.vertx.ceylon.core.stream {
  Pump
}
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
   
   
   
   """

by("Julien Viet")
license("ASL2")
module io.vertx.ceylon.platform "0.4.0" {

  shared import io.vertx.ceylon.core "0.4.0";

} 
