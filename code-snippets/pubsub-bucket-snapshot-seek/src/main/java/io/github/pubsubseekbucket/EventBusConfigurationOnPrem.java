package io.github.pubsubseekbucket;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import io.github.pubsubseekbucket.util.SubscriptionAdminUtil;

@Configuration
@Import(EventBusConfigurationBase.class)
public class EventBusConfigurationOnPrem {

    @Bean
    public SubscriptionAdminUtil subscriptionAdminUtil(@Value("${spring.cloud.gcp.pubsub.project-id}") String projectId,
                                                       CredentialsProvider credentialsProvider,
                                                       @Qualifier("subscriberTransportChannelProvider") TransportChannelProvider transportChannelProvider) {
        return new SubscriptionAdminUtil(projectId, credentialsProvider, transportChannelProvider);
    }
}
