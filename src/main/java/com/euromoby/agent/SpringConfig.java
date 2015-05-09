package com.euromoby.agent;

import java.beans.PropertyVetoException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.euromoby.database.ComboPooledDataSourceFactory;
import com.mchange.v2.c3p0.ComboPooledDataSource;


@Configuration
@ComponentScan("com.euromoby")
@EnableTransactionManagement
public class SpringConfig {
	@Bean
	public Config config() {
		return new Config();
	}
	
	@Bean(destroyMethod="shutdown")
	public EmbeddedDatabase dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		builder.setType(EmbeddedDatabaseType.H2);
		builder.setDataSourceFactory(comboPooledDataSourceFactory(config()));
		builder.addScript("classpath:com/euromoby/agent/schema.sql");
		return builder.build();
	}
	
	@Bean
	public DataSourceTransactionManager transactionManager() {
		DataSourceTransactionManager manager = new DataSourceTransactionManager();
		manager.setDataSource(dataSource());
		return manager;
	}
	
	public ComboPooledDataSourceFactory comboPooledDataSourceFactory(Config config) {
		return new ComboPooledDataSourceFactory(comboPooledDataSource(config));
	}
	
	public ComboPooledDataSource comboPooledDataSource(Config config) {
		String databasePath = config.getAgentDatabasePath();
		
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass("org.h2.Driver");
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}
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
