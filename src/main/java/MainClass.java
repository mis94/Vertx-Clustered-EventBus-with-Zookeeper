import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class MainClass {
    public static void main(String[] args) throws UnknownHostException {
        JsonObject zkConfig = new JsonObject();
        zkConfig.put("zookeeperHosts", "172.19.1.13");
        zkConfig.put("rootPath", "io.vertx");
        zkConfig.put("retry", new JsonObject()
                .put("initialSleepTime", 3000)
                .put("maxTimes", 3));
        ClusterManager zookeeperClusterManager = new ZookeeperClusterManager(zkConfig);

        // zookeeperClusterManager.setVertx(Vertx.vertx());
        // EventBusOptions eventBusOptions = configureEventBus();
        VertxOptions options = new VertxOptions()
               // .setEventBusOptions(configureEventBus())
                .setClustered(true)
                .setClusterHost(InetAddress.getLocalHost().getHostAddress())
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
                System.out.println("Hello from Vert.x instance deploying MainVerticle");
                vertx.deployVerticle("verticle.MainVerticle");
            }
        });
        System.out.println("#####################################");
    }

    private static EventBusOptions configureEventBus() {
        EventBusOptions eventBusOptions = new EventBusOptions()
                .setClusterPublicHost("172.19.1.13").setClusterPublicPort(8082);
        return eventBusOptions;
    }
}
