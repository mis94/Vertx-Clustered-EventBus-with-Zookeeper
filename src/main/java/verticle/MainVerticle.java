package verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Arrays;

public class MainVerticle extends AbstractVerticle {

    private HttpServer httpServer;
    private Router router;
    private EventBus eventBus;
    private ConfigRetriever configRetriever;
    private JsonObject configurations;
    Future<Void> configurationFileFuture = Future.future();

    @Override
    public void start() throws Exception { // consider implementing the asynchronous version of start method
        readConfigurationFile();
        assignEventBus();
        createHttpServer();
        createRouter();
        addHandlersToRouter();
        addWelcomePageRoute();
        addMessageRoute();
        configureRouterOnServer();
        listen();
    }

    private void readConfigurationFile() {
        ConfigStoreOptions file = new ConfigStoreOptions()
                .setType("file")
                .setFormat("json")
                .setConfig(new JsonObject().put("path", "src/main/conf/applicationconfig.json"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(file);
        configRetriever = ConfigRetriever.create(vertx, options);
        configRetriever.getConfig(jsonObjectAsyncResult -> {
            if(jsonObjectAsyncResult.failed()) {
                // set configurations object to null
                configurationFileFuture.fail("Failed to read configuration file.");
            }else {
                configurations = jsonObjectAsyncResult.result();
                configurationFileFuture.complete();
            }
        });
    }

    private void assignEventBus() {
        eventBus = vertx.eventBus();
    }

    private void createHttpServer() {
        httpServer = vertx.createHttpServer();
    }

    private void createRouter() {
        router = Router.router(vertx);
    }

    private void addHandlersToRouter() {
        router.route().handler(BodyHandler.create());
    }

    private void addWelcomePageRoute() {
        router.route("/").handler(routingContext -> {
            HttpServerResponse httpServerResponse = routingContext.response();
            httpServerResponse.setChunked(true);
            httpServerResponse.write("Welcome to user management system");
            routingContext.response().end();
        });
    }

    private void addMessageRoute() {
        router.route(HttpMethod.POST, "/weather").handler(routingContext -> {
            System.out.println("handler started!");
            JsonObject jsonMessage = routingContext.getBodyAsJson();
            eventBus.send("Weather", jsonMessage, messageAsyncResult -> {
                if(messageAsyncResult.succeeded()) {
                    JsonObject jsonReply = (JsonObject) messageAsyncResult.result().body();
                    System.out.println("received reply: " + jsonReply.getValue("reply"));
                    routingContext.response().end(jsonReply.toBuffer()); // jsonReply.toString() also works
                }
            });
        });
    }

    private void configureRouterOnServer() {
        httpServer.requestHandler(router::accept);
    }

    private void listen() {
        CompositeFuture.all(Arrays.asList(configurationFileFuture)).setHandler(compositeFutureAsyncResult -> {
            if(compositeFutureAsyncResult.failed()) {
                httpServer.listen(8080); // a default value
            }else {
                httpServer.listen(configurations.getInteger("server.port")); // port value read from configuration file
            }
        });
    }
}
