package com.euromoby.agent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.euromoby.service.ServiceManager;


@Configuration
@ComponentScan("com.euromoby")
public class AgentSpringConfig {

	@Bean
	public Agent agent() {
		Agent agent = new Agent(config());
		agent.initServices();
		return agent;
	}
	
	@Bean
	public Config config() {
		return new Config();
	}
	
	@Bean
	public ServiceManager serviceManager() {
		ServiceManager serviceManager = new ServiceManager();
		return serviceManager;
	}
	
}
