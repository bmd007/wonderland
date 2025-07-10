package wonderland.authentication.config;

import com.xebialabs.restito.server.StubServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.util.List;

@Configuration
@ActiveProfiles("test")
public class ClientSideLoadBalancerConfig {

    @Autowired
    StubServer stubServer;

    @Bean
    ServiceInstanceListSupplier serviceInstanceListSupplier() {
        return new ServiceInstanceListSupplier() {

            @Override
            public String getServiceId() {
                return "";
            }

            @Override
            public Flux<List<ServiceInstance>> get() {
                return Flux.just(List.of(new DefaultServiceInstance("", "", "localhost", stubServer.getPort(), false)));
            }
        };
    }
}
