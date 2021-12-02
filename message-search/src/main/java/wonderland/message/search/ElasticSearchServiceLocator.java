package wonderland.message.search;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.stream.Collectors.toList;

//@Component
public class ElasticSearchServiceLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchServiceLocator.class);
    private static final Long REFRESH_TIME_NANO = Duration.ofHours(5).toNanos();

    private DiscoveryClient discoveryClient;
    private RestHighLevelClient client;
    private AtomicLong lastUpdated = new AtomicLong(System.nanoTime());
    private int port = 9200;
    private String hostIp;

    public ElasticSearchServiceLocator(DiscoveryClient discoveryClient,
                                       @Value("${core-elasticsearch.bootstrap.servers.http:9200}") String portHolder) {
        this.discoveryClient = discoveryClient;
        // for tests, the embedded elastic search will only put a port in the property instead of
        // localhost:9200
        // so the code below need to be ugly
        if (portHolder.contains(",")) {
            port = Integer.valueOf(portHolder.split(",")[0].split(":")[1]);
            hostIp = portHolder.split(",")[0].split(":")[0];
        } else if (!portHolder.contains(",") && portHolder.contains(":")) {
            port = Integer.valueOf(portHolder.split(":")[1]);
            hostIp = portHolder.split(":")[0];
        } else {
            port = Integer.valueOf(portHolder);
            hostIp = "localhost";
        }
        refreshClientAddresses();
    }

    public RestHighLevelClient getClient() {
        if (lastUpdated.get() + REFRESH_TIME_NANO < System.nanoTime()) {
            refreshClientAddresses();
            lastUpdated.set(System.nanoTime());
        }
        return client;
    }

    private synchronized void refreshClientAddresses() {
        var httpHosts = discoverElasticSearchAddress();
        if (!httpHosts.isEmpty()) {
            reinitializeClient(httpHosts);
        } else {
            LOGGER.error("No Elasticsearch hosts found");
        }
    }

    private void reinitializeClient(List<HttpHost> httpHosts) {
        if (client != null) {
            try {
                client.close();
            } catch (Throwable e) {
                LOGGER.error("Couldn't close elastic search client.", e);
            }
        }
        LOGGER.info("Initializing elasticsearch client with hosts: {}", httpHosts);
        client = new RestHighLevelClient(RestClient.builder(httpHosts.get(0)));
    }

    private List<HttpHost> discoverElasticSearchAddress() {
        var foundedHosts = discoveryClient.getInstances("elasticsearch").stream()
                .map(instance -> new HttpHost(instance.getHost(), port, "http"))
                .collect(toList());
        return foundedHosts.isEmpty() ? List.of(new HttpHost(hostIp, port, "http")) : foundedHosts;
    }
}