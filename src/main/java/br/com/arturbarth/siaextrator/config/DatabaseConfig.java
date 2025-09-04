package br.com.arturbarth.siaextrator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "br.com.arturbarth.siaextrator.repository")
@EnableTransactionManagement
public class DatabaseConfig {
}
