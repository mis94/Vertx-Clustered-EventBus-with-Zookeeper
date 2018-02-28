import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class MainClass {
    private static ConfigRetriever configRetriever;
    private static JsonObject configurations;

    public static void main(String[] args) throws UnknownHostException {
        configureConfigRetriever();
        Future<JsonObject> future = ConfigRetriever.getConfigAsFuture(configRetriever);
        future.setHandler(jsonObjectAsyncResult -> {
            if(jsonObjectAsyncResult.failed()) {
                System.out.println("failure");
            }else {
                configurations = jsonObjectAsyncResult.result();
                JsonObject zkConfig = configureClusterManager();
                ClusterManager zookeeperClusterManager = new ZookeeperClusterManager(zkConfig);

                VertxOptions options = configureVertx(zookeeperClusterManager);
                Vertx.clusteredVertx(options, res -> {
                    if (res.succeeded()) {
                        Vertx vertx = res.result();
                        System.out.println("Hello from Vert.x instance deploying MainVerticle");
                        vertx.deployVerticle("verticle.MainVerticle");
                    }
                });
                System.out.println("#####################################");
            }
        });
    }

    private static void configureConfigRetriever() {
        ConfigStoreOptions file = new ConfigStoreOptions()
                .setType("file")
                .setFormat("json")
                .setConfig(new JsonObject().put("path", "src/main/conf/applicationconfig.json"));
        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(file);
        configRetriever = ConfigRetriever.create(Vertx.vertx(), options);
    }

    private static JsonObject configureClusterManager() {
        // add the IP of the machine hosting the cluster manager
        JsonObject zkConfig = new JsonObject();
        zkConfig.put("zookeeperHosts", configurations.getString("zookeeper.host"));
        zkConfig.put("rootPath", "io.vertx");
        zkConfig.put("retry", new JsonObject()
                .put("initialSleepTime", configurations.getInteger("zookeeper.initialSleepTime"))
                .put("maxTimes", configurations.getInteger("zookeeper.maxTimes")));
        return zkConfig;
    }

    private static VertxOptions configureVertx(ClusterManager clusterManager) {
        // add this machine's IP as clusterHost
        // if vertx instances run on the same machine then we must use different port for each instance
        VertxOptions options = new VertxOptions()
                .setClustered(true)
                .setClusterHost(configurations.getString("cluster.host")) // InetAddress.getLocalHost().getHostAddress()
                .setClusterPort(configurations.getInteger("cluster.port1"))
                .setClusterManager(clusterManager);
        return options;
    }
}
