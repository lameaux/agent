package com.euromoby.agent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan("com.euromoby")
public class AgentSpringConfig {
	@Bean
	public Config config() {
		return new Config();
	}
}
