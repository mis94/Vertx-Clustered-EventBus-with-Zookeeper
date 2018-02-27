import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;

import java.util.Arrays;

public class MainClass {
    public static void main(String[] args) {
        IgniteConfiguration cfg = configureIgnite();
        ClusterManager igniteClusterManager = new IgniteClusterManager(cfg);

//        EventBusOptions eventBusOptions = configureEventBus();
        VertxOptions options = new VertxOptions().setClusterHost("172.19.1.56").setClusterPort(8082)
                .setClusterPublicHost("172.19.1.56")
                .setClusterManager(igniteClusterManager);

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

    private static IgniteConfiguration configureIgnite() {
        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        // Set Multicast group.
//        ipFinder.setMulticastGroup("228.10.10.157");
        // Set initial IP addresses.
        // Note that you can optionally specify a port or a port range.
        // here add the address of the other machine
        ipFinder.setAddresses(Arrays.asList("172.19.1.56", "172.19.1.56:47500..47509", "172.19.1.186", "172.19.1.186:47500..47509"));
        spi.setIpFinder(ipFinder);
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setDiscoverySpi(spi);
        return cfg;
    }
}
