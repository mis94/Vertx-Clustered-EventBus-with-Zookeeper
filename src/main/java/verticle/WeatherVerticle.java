package verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class WeatherVerticle extends AbstractVerticle {

    private String verticleAddress = "Weather";
    private EventBus eventBus;

    @Override
    public void start() throws Exception {
        assignEventBus();
        registerHandler();
    }

    private void assignEventBus() {
        eventBus = vertx.eventBus();
    }

//    private void registerHandler() {
//        MessageConsumer<JsonObject> messageConsumer = eventBus.consumer(verticleAddress);
//        messageConsumer.handler(message -> {
//            // because we made MessageConsumer<JsonObject> so all message instances are of type JsonObject
//            // we could have made it MessageConsumer<String> so that all message instances would have been of type String
//            JsonObject jsonMessage = message.body();
//            System.out.println(jsonMessage.getValue("message_from_postman"));
//            JsonObject jsonReply = new JsonObject();
//            jsonReply.put("reply", "how interesting!");
//            message.reply(jsonReply);
//        });
//    }

    private void registerHandler() {
        MessageConsumer<JsonObject> messageConsumer = eventBus.consumer(verticleAddress);
        messageConsumer.handler(message -> {
            // because we made MessageConsumer<JsonObject> so all message instances are of type JsonObject
            // we could have made it MessageConsumer<String> so that all message instances would have been of type String
            JsonObject jsonMessage = message.body();
            System.out.println(jsonMessage.getValue("message_from_postman"));
            WebClient webClient = WebClient.create(vertx);
            webClient.get(443, "jsonplaceholder.typicode.com", "/posts/1").ssl(true).as(BodyCodec.jsonObject())
                    .send(httpResponseAsyncResult -> {
                        if(httpResponseAsyncResult.succeeded()) {
                            JsonObject jsonReply = httpResponseAsyncResult.result().body();
                            jsonReply.put("reply", "how interesting!");
                            message.reply(jsonReply);
                        }else {
                            System.out.println(httpResponseAsyncResult.cause());
                        }
                    });
        });
    }
}
