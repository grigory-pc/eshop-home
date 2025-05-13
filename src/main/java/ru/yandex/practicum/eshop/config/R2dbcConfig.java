package ru.yandex.practicum.eshop.config;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

@Configuration
public class R2dbcConfig {

  @Bean
  ConnectionFactory connectionFactory(
      @Value("${spring.r2dbc.username}") String username,
      @Value("${spring.r2dbc.password}") String password
  ) {
    return ConnectionFactories.get(
        ConnectionFactoryOptions.builder()
                                .option(DRIVER, "h2")
                                .option(HOST, "localhost")
                                .option(PORT, 15215)
                                .option(DATABASE, "testdb")
                                .option(USER, username)
                                .option(PASSWORD, password)
                                .build()
    );
  }
}