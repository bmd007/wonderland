
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se..companydata.keyfigures.config.RestCallProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "rest-clients.instrument-domain")
public record InstrumentDomainProperties(@NonNull URI baseUrl, RestCallProperties shortSales) {
}
