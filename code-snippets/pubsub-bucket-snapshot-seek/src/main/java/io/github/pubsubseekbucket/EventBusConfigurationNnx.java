package io.github.pubsubseekbucket;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import io.github.pubsubseekbucket.util.SubscriptionAdminUtil;

@Configuration
@Import(EventBusConfigurationBase.class)
@ConditionalOnProperty(value = "spring.cloud.gcp.pubsub.enabled", matchIfMissing = true)
public class EventBusConfigurationNnx {

    @Bean
    public SubscriptionAdminUtil subscriptionAdminUtilForNNX(GcpProjectIdProvider projectIdProvider,
                                                             CredentialsProvider credentialsProvider,
                                                             @Qualifier("subscriberTransportChannelProvider") TransportChannelProvider transportChannelProvider) {
        return new SubscriptionAdminUtil(projectIdProvider.getProjectId(), credentialsProvider, transportChannelProvider);
    }


}
