package com.euromoby.agent;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Agent {

	public static final String TITLE = "Agent";
	public static final String VERSION = "0.1";

	public static void main(String[] args) {
		// setup netty logger
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		// spring configuration
		ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(AgentSpringConfig.class);

		// shutdown hook
		Runtime.getRuntime().addShutdownHook(new AgentShutdownHook(ctx));

		// prevent from close
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
}

class AgentShutdownHook extends Thread {
	private ConfigurableApplicationContext ctx;

	public AgentShutdownHook(ConfigurableApplicationContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void run() {
		ctx.close();
	}
}
