import io.vertx.ceylon.platform {
  Verticle,
  Container
}
import io.vertx.ceylon.core {
  Vertx
}
import io.vertx.ceylon.core.eventbus {
  EventBus,
  Message
}
import ceylon.json { JsonObject = Object, JsonArray = Array }
shared class App() extends Verticle() {
  
  shared actual void start(Vertx vertx, Container container) {
    
    // Configuration for the web server
    value webServerConf = JsonObject {
      
      // Normal web server stuff
      "port"->8080,
      "host"->"localhost",
      "ssl"->true,
      
      // Configuration for the event bus client side bridge
      // This bridges messages from the client side to the server side event bus
      "bridge"->true,
      
      // This defines which messages from the client we will let through
      // to the server side
      "inbound_permitted"->JsonArray {
        // Allow calls to login
        JsonObject {
          "address"->"vertx.basicauthmanager.login"
        },
        // Allow calls to get static album data from the persistor
        JsonObject {
          "address"->"vertx.mongopersistor",
          "match"->JsonObject {
            "action"->"find",
            "collection"->"albums"
          }
        },
        // And to place orders
        JsonObject {
          "address"->"vertx.mongopersistor",
          "requires_auth"->true,
          "match"->JsonObject {
            "action"->"save",
            "collection"->"orders"
          }
        }
      },
      
      // This defines which messages from the server we will let through to the client
      "outbound_permitted"->JsonArray {
        JsonObject { }
      }
    };

    // Now we deploy the modules that we need
    
    value deployed = container.deployModule("io.vertx~mod-mongo-persistor~2.0.0-final");
    deployed.onComplete {
      void onFulfilled(String id) {
        importStaticData(vertx.eventBus);
      }
      void onRejected(Throwable cause) {
        print("Failed to deploy:");
        cause.printStackTrace();
      }
    };

    container.deployModule("io.vertx~mod-auth-mgr~2.0.0-final");
    
    container.deployModule("io.vertx~mod-web-server~2.0.0-final", webServerConf).onComplete {
      void onFulfilled(String id) {
        print("deployed webserver");
      }
      void onRejected(Throwable cause) {
        print("could not deploy webserver");
        cause.printStackTrace();
      }
    };
  }  
  
  void importStaticData(EventBus eb) {
    value pa = "vertx.mongopersistor";
    
    value albums = JsonArray {
      JsonObject {
        "artist" -> "The Wurzels",
        "genre" -> "Scrumpy and Western",
        "title" -> "I Am A Cider Drinker",
        "price" -> 0.99
      },
      JsonObject {
        "artist" -> "Vanilla Ice",
        "genre" -> "Hip Hop",
        "title" -> "Ice Ice Baby",
        "price" -> 0.01
      },
      JsonObject {
        "artist" -> "Ena Baga",
        "genre" -> "Easy Listening",
        "title" -> "The Happy Hammond",
        "price" -> 0.50
      },
      JsonObject {
        "artist" -> "The Tweets",
        "genre" -> "Bird related songs",
        "title" -> "The Birdy Song",
        "price" -> 1.20
      }
    };
    
    // First delete albums
    eb.send<JsonObject>(pa, JsonObject { "action"->"delete", "collection"->"albums", "matcher"->JsonObject()}).onComplete {
      void onFulfilled(Message<JsonObject> repl) {
        for (album in albums) {
          eb.send(pa, JsonObject {
            "action"->"save",
            "collection"->"albums",
            "document"->album
          });
        }
      }
    };
    
    // Delete users
    eb.send<JsonObject>(pa, JsonObject { "action"->"delete", "collection"->"users", "matcher"->JsonObject()}).onComplete {
      void onFulfilled(Message<JsonObject> repl) {
        for (album in albums) {
          eb.send(pa, JsonObject {
            "action"->"save",
            "collection"->"users",
            "document"->JsonObject {
              "firstname"->"Tim",
              "lastname"->"Fox",
              "email"->"tim@localhost.com",
              "username"->"tim",
              "password"->"password"
            }
          });
        }
      }
    };
  }
}