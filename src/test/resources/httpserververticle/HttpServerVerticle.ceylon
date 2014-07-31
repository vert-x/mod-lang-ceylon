import io.vertx.ceylon.platform {
	Verticle,
	Container
}
import io.vertx.ceylon.http { HttpServerRequest }
import io.vertx.ceylon {
	Vertx
}

shared class HttpServerVerticle() extends Verticle() {
	shared actual void start(Vertx vertx, Container container) {
		value server = vertx.createHttpServer();
		server.requestHandler((HttpServerRequest req) =>
			req.response.contentType("text/html").end("<html><body>Hello World</body></html>")
		);
		server.listen(8080);
	}
}
