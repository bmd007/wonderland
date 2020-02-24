package wonderland.helloyee;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import rx.functions.Func1;

import java.util.UUID;

@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced
    @Qualifier("loadBalancedClient")
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @Qualifier("notLoadBalancedClient")
    public WebClient.Builder notLoadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

}
