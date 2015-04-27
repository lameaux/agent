package com.euromoby.agent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.mchange.v2.c3p0.ComboPooledDataSource;


@Configuration
@ComponentScan("com.euromoby")
public class AgentSpringConfig {
	@Bean
	public Config config() {
		return new Config();
	}
	
	@Bean(destroyMethod="close")
	public ComboPooledDataSource dataSource() throws Exception {
		Config config = config();
		String databasePath = config.getAgentDatabasePath();
		
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass("org.h2.Driver");
		dataSource.setJdbcUrl("jdbc:h2:" + databasePath);
		dataSource.setUser("SA");
		dataSource.setPassword("");
		dataSource.setInitialPoolSize(10);
		dataSource.setMinPoolSize(10);
		dataSource.setMaxPoolSize(50);
		dataSource.setMaxStatements(100);
		dataSource.setMaxIdleTime(10000);
		return dataSource;
	}
	
}
