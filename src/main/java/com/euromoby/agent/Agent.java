package com.euromoby.agent;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.euromoby.job.JobManager;
import com.euromoby.job.JobScheduler;
import com.euromoby.model.AgentId;
import com.euromoby.ping.PingScheduler;
import com.euromoby.processor.CommandProcessor;
import com.euromoby.rest.RestServer;
import com.euromoby.service.ServiceManager;
import com.euromoby.telnet.TelnetServer;
import com.euromoby.utils.NetUtils;


public class Agent {

	public static final String TITLE = "Agent";
	public static final String VERSION = "0.1";

	public static Agent instance;
	
	private Config config = new Config();
	private ServiceManager serviceManager;
	private JobManager jobManager;
	private AgentManager agentManager;
	

	private CommandProcessor commandProcessor;

	public Agent(Config config) {
		this.config = config;		
	}

	public void initServices() {

		// managers
		agentManager = new AgentManager();
		jobManager = new JobManager();
		serviceManager = new ServiceManager();

		// CommandProcessor should be the last
		commandProcessor = new CommandProcessor();

		TelnetServer telnetServer = new TelnetServer();
		serviceManager.registerService(telnetServer);

		RestServer restServer = new RestServer();
		serviceManager.registerService(restServer);
		
		// Schedulers
		JobScheduler jobScheduler = new JobScheduler();
		serviceManager.registerService(jobScheduler);

		PingScheduler pingScheduler = new PingScheduler();
		serviceManager.registerService(pingScheduler);
		
		for (String serviceName : config.getAutorunServices()) {
			serviceManager.executeAction(serviceName, ServiceManager.ACTION_START);
		}
	}


	public AgentId getAgentId() {
		return new AgentId(NetUtils.getHostname(), config.getBasePort());
	}

	public Config getConfig() {
		return config;
	}

	public ServiceManager getServiceManager111() {
		return serviceManager;
	}

	public CommandProcessor getCommandProcessor() {
		return commandProcessor;
	}

	public JobManager getJobManager() {
		return jobManager;
	}
	
	public AgentManager getAgentManager() {
		return agentManager;
	}

	public static Agent get() {
		return instance;
	}
	
	public static void main(String[] args) {
		// setup netty logger
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());		

		// spring configuration
		ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(AgentSpringConfig.class);		
		
		// shutdown hook
		Runtime.getRuntime().addShutdownHook(new AgentShutdownHook(ctx));

		instance = ctx.getBean(Agent.class);
		
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

