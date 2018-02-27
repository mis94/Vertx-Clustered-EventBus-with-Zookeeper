import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;

public class MainClass2 {
    public static void main(String[] args) {
        // add the IP of the machine hosting the cluster manager
        JsonObject zkConfig = new JsonObject();
        zkConfig.put("zookeeperHosts", "172.19.1.13");
        zkConfig.put("rootPath", "io.vertx");
        zkConfig.put("retry", new JsonObject()
                .put("initialSleepTime", 3000)
                .put("maxTimes", 3));
        ClusterManager zookeeperClusterManager = new ZookeeperClusterManager(zkConfig);

        // add my IP as clusterHost
        // EventBusOptions eventBusOptions = configureEventBus();
        VertxOptions options = new VertxOptions()
                .setClustered(true)
                .setClusterHost("172.19.1.56")
                .setClusterPort(17001)
                .setClusterManager(zookeeperClusterManager);

        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                System.out.println("options.isClustered() " + options.isClustered());
                System.out.println("options.getClusterHost() " + options.getClusterHost());
                System.out.println("options.getClusterPort() " + options.getClusterPort());
                System.out.println("options.getClusterPublicHost() " + options.getClusterPublicHost());
                System.out.println("options.getClusterPublicPort() " + options.getClusterPublicPort());
                EventBusOptions vertxEventBusOptions = options.getEventBusOptions();
                System.out.println("vertxEventBusOptions.getHost() " + vertxEventBusOptions.getHost());
                System.out.println("vertxEventBusOptions.getPort() " + vertxEventBusOptions.getPort());
                System.out.println("vertxEventBusOptions.getClusterPublicHost() " + vertxEventBusOptions.getClusterPublicHost());
                System.out.println("vertxEventBusOptions.getClusterPublicPort() " + vertxEventBusOptions.getClusterPublicPort());
                System.out.println("Hello from Vert.x instance deploying WeatherVerticle");
                vertx.deployVerticle("verticle.WeatherVerticle");
            }
        });
        System.out.println("#####################################");
    }
}
