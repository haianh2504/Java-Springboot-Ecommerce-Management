package configuration;

import common.DatabaseConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shipping.FlatRateShippingStrategy;
import shipping.ShippingStrategy;

import java.math.BigDecimal;
import java.sql.Connection;

@Configuration
public class InfrastructureConfiguration {

    @Bean(destroyMethod = "close")
    public Connection databaseConnection() {
        return DatabaseConnection.getConnection();
    }

    @Bean
    public ShippingStrategy shippingStrategy(
            @Value("${shipping.flat-fee:30000}") BigDecimal flatFee
    ) {
        return new FlatRateShippingStrategy(flatFee);
    }
}
