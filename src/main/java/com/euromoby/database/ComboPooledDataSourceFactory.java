package com.euromoby.database;

import java.sql.Driver;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.DataSourceFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ComboPooledDataSourceFactory implements DataSourceFactory {

	private final ComboPooledDataSource dataSource;

	public ComboPooledDataSourceFactory(ComboPooledDataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}	
	
	@Override
	public ConnectionProperties getConnectionProperties() {
		return new ConnectionProperties() {
			
			@Override
			public void setUsername(String username) {
			}
			
			@Override
			public void setUrl(String url) {
			}
			
			@Override
			public void setPassword(String password) {
			}
			
			@Override
			public void setDriverClass(Class<? extends Driver> driverClass) {
			}
		};
	}
}
