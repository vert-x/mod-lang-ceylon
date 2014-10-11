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

   ## The container object
   
   When the verticle starts it gets a [[Container]] instance. This represents the Verticle's view of the container in which it is running.
   
   The container object contains methods for deploying and undeploying verticle and modules, and also allows config,
   environment variables and a logger to be accessed.
   
   ## The vertx object
   
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
   
   Each verticle is given its own logger. To get a reference to it use the [[Container.logger]]
   object on the container instance:

   ~~~
   value logger = container.logger;
   
   logger.info("I am logging something");
   ~~~
   
   The logger is an instance of the interface [[ceylon.logging::Logger]] and has the following methods:
   
   - [[ceylon.logging::Logger.trace]]
   - [[ceylon.logging::Logger.debug]]
   - [[ceylon.logging::Logger.info]]
   - [[ceylon.logging::Logger.warn]]
   - [[ceylon.logging::Logger.error]]
   - [[ceylon.logging::Logger.fatal]]
   
   Which have the normal meanings you would expect.
   
   The log files by default go in a file called `vertx.log in the system temp directory. On my Linux box this is \tmp.
   
   For more information on configuring logging, please see the [main manual](http://vertx.io/manual.html#logging).
   
   ## Accessing environment variables from a Verticle
   
   You can access the map of environment variables from a Verticle with the [[Container.env]] object on the `container` object.
   
   ## Causing the container to exit
   
   You can call the [[Container.exit]] method of the container to cause the Vert.x instance to make a clean shutdown.
   
   # Deploying and Undeploying Verticles Programmatically
   
   You can deploy and undeploy verticles programmatically from inside another verticle. Any verticles deployed this
   way will be able to see resources (classes, scripts, other files) of the main verticle.
   
   ## Deploying a simple verticle
   
   To deploy a verticle programmatically call the function [[Container.deployVerticle]] on the container.
   
   To deploy a single instance of a verticle :
   
   ~~~
   container.deployVerticle(main);
   ~~~
   
   Where `main` is the name of the Verticle (i.e. the name of the Java file or FQCN of the class).
   
   See the chapter on ["running Vert.x"](http://vertx.io/manual.html#running-vertx) in the main manual for a description
   of what a main is.
   
   ## Deploying Worker Verticles
   
   The [[Container.deployVerticle]] method deploys standard (non worker) verticles. If you want to deploy worker
   verticles use the [[Container.deployWorkerVerticle]] method. This method takes the same parameters as [[Container.deployVerticle]]
   with the same meanings.
   
   ## Deploying a module programmatically
   
   You should use [[Container.deployModule]] to deploy a module, for example:
   
   ~~~
   container.deployModule("io.vertx~mod-mailer~2.0.0-beta1", config);
   ~~~
   
   Would deploy an instance of the `io.vertx~mod-mailer~2.0.0-beta1` module with the specified configuration. Please
   see the modules manual for more information about modules.
   
   ## Passing configuration to a verticle programmatically
   
   JSON configuration can be passed to a verticle that is deployed programmatically. Inside the deployed verticle the
   configuration is accessed with the [[Container.config]] attribute. For example:
   
   ~~~
   value config = Object {
     "foo"->"wibble",
     "bar"->false
   };
   container.deployVerticle("foo.ChildVerticle", config);
   ~~~
   
   Then, in `ChildVerticle` you can access the config via [[Container.config]] as previously explained.
   
   ## Using a Verticle to co-ordinate loading of an application
   
   If you have an appplication that is composed of multiple verticles that all need to be started at application start-up,
   then you can use another verticle that maintains the application configuration and starts all the other verticles. You
   can think of this as your application starter verticle.
   
   For example, you could create a verticle AppStarter as follows:
   
   ~~~
   // Application config
   
   JsonObject appConfig = container.config();
   
   value verticle1Config = appConfig["verticle1_conf"];
   value verticle2Config = appConfig["verticle2_conf"];
   value verticle3Config = appConfig["verticle3_conf"];
   value verticle4Config = appConfig["verticle4_conf"];
   value verticle5Config = appConfig["verticle5_conf"];
   
   // Start the verticles that make up the app
   
   container.deployVerticle("verticle1.js", verticle1Config);
   container.deployVerticle("verticle2.rb", verticle2Config);
   container.deployVerticle("foo.Verticle3", verticle3Config);
   container.deployWorkerVerticle("foo.Verticle4", verticle4Config);
   container.deployWorkerVerticle("verticle5.js", verticle5Config, 10);
   ~~~
   
   Then create a file 'config.json" with the actual JSON config in it
   
   ~~~
   {
      "verticle1_conf": {
          "foo": "wibble"
      },
      "verticle2_conf": {
          "age": 1234,
          "shoe_size": 12,
          "pi": 3.14159
      },
      "verticle3_conf": {
          "strange": true
      },
      "verticle4_conf": {
          "name": "george"
      },
      "verticle5_conf": {
          "tel_no": "123123123"
      }
   }
   ~~~
   
   Then set the `AppStarter` as the main of your module and then you can start your entire application by simply running:
   
   ~~~
   vertx runmod com.mycompany~my-mod~1.0 -conf config.json
   ~~~
   
   If your application is large and actually composed of multiple modules rather than verticles you can use the same technique.
   
   More commonly you'd probably choose to write your starter verticle in a scripting language such as JavaScript, Groovy,
   Ruby or Python - these languages have much better JSON support than Java, so you can maintain the whole JSON config nicely
   in the starter verticle itself.
   
   ## Specifying number of instances
   
   By default, when you deploy a verticle only one instance of the verticle is deployed. Verticles instances are
   strictly single threaded so this means you will use at most one core on your server.
   
   Vert.x scales by deploying many verticle instances concurrently.
   
   If you want more than one instance of a particular verticle or module to be deployed, you can specify the number of
   instances as follows:
   
   ~~~
   container.deployVerticle("foo.ChildVerticle", null, 10);
   ~~~
   
   or

   ~~~
   container.deployModule("io.vertx~some-mod~1.0", null, 10);
   ~~~
   
   The above examples would deploy 10 instances.
   
   ## Getting Notified when Deployment is complete
   
   The actual verticle deployment is asynchronous and might not complete until some time after the call to
   [[Container.deployVerticle]] or [[Container.deployVerticle]] has returned. If you want to be notified when
   the verticle has completed being deployed, you can use the [[ceylon.promise::Promise]] returned by
   [[Container.deployVerticle]] or [[Container.deployModule]]:

   ~~~
   Promise<String> deployment = container.deployVerticle("foo.ChildVerticle");
   deployment.onComplete(
     (String deploymentID) => print("The verticle has been deployed, deployment ID is ``deploymentID``"),
     (Throwable failure) => failure.printStackTrace()
   );
   ~~~
   
   The promise is resolved with the `deploymentID`, you will need this if you need to subsequently undeploy the
   verticle / module.
   
   ## Undeploying a Verticle or Module
   
   Any verticles or modules that you deploy programmatically from within a verticle, and all of their children are
   automatically undeployed when the parent verticle is undeployed, so in many cases you will not need to undeploy
   a verticle manually, however if you do need to do this, it can be done by calling the method [[Container.undeployVerticle]]
   or [[Container.undeployModule]] passing in the deployment id.
   
   ~~~
   container.undeployVerticle(deploymentID);
   ~~~
   
   You can use the returned promise if you want to be informed when undeployment is complete.
   
   ## Scaling your application
   
   A verticle instance is almost always single threaded (the only exception is multi-threaded worker verticles which
   are an advanced feature), this means a single instance can at most utilise one core of your server.
   
   In order to scale across cores you need to deploy more verticle instances. The exact numbers depend on your
   application - how many verticles there are and of what type.

   You can deploy more verticle instances programmatically or on the command line when deploying your
   module using the -instances command line option.
   """
by ("Julien Viet")
license ("ASL2")
module io.vertx.ceylon.platform "1.0.0" {
  
  shared import ceylon.logging "1.1.0";
  shared import io.vertx.ceylon.core "1.0.0";
}
