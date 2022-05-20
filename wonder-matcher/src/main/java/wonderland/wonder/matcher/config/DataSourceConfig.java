package wonderland.wonder.matcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        return new SimpleDriverDataSource(new org.h2.Driver(), "jdbc:h2:nioMemFS:movers;DB_CLOSE_DELAY=-1;" + "LOCK_TIMEOUT="+10000);
    }
}
