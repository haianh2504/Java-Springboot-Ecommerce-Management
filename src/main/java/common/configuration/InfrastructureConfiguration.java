package common.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import shipping.FlatRateShippingStrategy;
import shipping.ShippingStrategy;

import javax.sql.DataSource;
import java.math.BigDecimal;

@Configuration
public class InfrastructureConfiguration {

    // Quản lý transaction JDBC bằng DataSource và tái sử dụng cùng một connection trong transaction hiện tại.
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public ShippingStrategy shippingStrategy(
            @Value("${shipping.flat-fee:30000}") BigDecimal flatFee
    ) {
        return new FlatRateShippingStrategy(flatFee);
    }
}
