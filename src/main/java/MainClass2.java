import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class MainClass2 {
    public static void main(String[] args) throws UnknownHostException {
        JsonObject zkConfig = configureClusterManager();
        ClusterManager zookeeperClusterManager = new ZookeeperClusterManager(zkConfig);

        VertxOptions options = configureVertx(zookeeperClusterManager);
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                System.out.println("Hello from Vert.x instance deploying WeatherVerticle");
                vertx.deployVerticle("verticle.WeatherVerticle");
            }
        });
        System.out.println("#####################################");
    }

    private static JsonObject configureClusterManager() {
        // add the IP of the machine hosting the cluster manager
        JsonObject zkConfig = new JsonObject();
        zkConfig.put("zookeeperHosts", "172.19.1.56");
        zkConfig.put("rootPath", "io.vertx");
        zkConfig.put("retry", new JsonObject()
                .put("initialSleepTime", 3000)
                .put("maxTimes", 3));
        return zkConfig;
    }

    private static VertxOptions configureVertx(ClusterManager clusterManager) {
        // add this machine's IP as clusterHost
        // if vertx instances run on the same machine then we must use different port for each instance
        VertxOptions options = new VertxOptions()
                .setClustered(true)
                .setClusterHost("172.19.1.56") // InetAddress.getLocalHost().getHostAddress()
                .setClusterPort(17002)
                .setClusterManager(clusterManager);
        return options;
    }
}
