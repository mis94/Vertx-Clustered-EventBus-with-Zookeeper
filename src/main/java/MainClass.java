import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkImpl;


public class MainClass {
    public static void main(String[] args) {
        ClusterManager zookeeperClusterManager = new ZookeeperClusterManager();

        // EventBusOptions eventBusOptions = configureEventBus();
        VertxOptions options = new VertxOptions().setClusterManager(zookeeperClusterManager);

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
                .setClusterPublicHost("172.19.1.56").setClusterPublicPort(8082);
        return eventBusOptions;
    }
}
