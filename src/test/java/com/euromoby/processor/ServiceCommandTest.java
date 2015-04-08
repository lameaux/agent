package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.service.ServiceManager;
import com.euromoby.service.ServiceState;
import com.euromoby.utils.StringUtils;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCommandTest {

	public static final String SERVICE_NAME = "service";
	
	@Mock
	ServiceManager serviceManager;
	ServiceCommand serviceCommand;

	@Before
	public void init() {
		serviceCommand = new ServiceCommand();
		serviceCommand.setServiceManager(serviceManager);
	}

	@Test
	public void testMatchName() {
		assertTrue(serviceCommand.match(ServiceCommand.NAME));
	}

	@Test
	public void testMatchWithParam() {
		assertTrue(serviceCommand.match(ServiceCommand.NAME + Command.SEPARATOR + ServiceManager.ACTION_STATUS + Command.SEPARATOR + SERVICE_NAME));
		assertTrue(serviceCommand.match(ServiceCommand.NAME + Command.SEPARATOR + ServiceManager.ACTION_STOP + Command.SEPARATOR + SERVICE_NAME));
		assertTrue(serviceCommand.match(ServiceCommand.NAME + Command.SEPARATOR + ServiceManager.ACTION_RESTART + Command.SEPARATOR + SERVICE_NAME));		
		assertTrue(serviceCommand.match(ServiceCommand.NAME + Command.SEPARATOR + ServiceManager.ACTION_START + Command.SEPARATOR + SERVICE_NAME));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(serviceCommand.match(ServiceCommand.NAME + "aaa"));
	}

	@Test
	public void testBadRequest() {
		String result = serviceCommand.execute(ServiceCommand.NAME + Command.SEPARATOR + "aaa");
		assertEquals(serviceCommand.syntaxError(), result);
	}

	@Test
	public void testBadRequest2() {
		String result = serviceCommand.execute(ServiceCommand.NAME + Command.SEPARATOR + " " + Command.SEPARATOR + " ");
		assertEquals(serviceCommand.syntaxError(), result);
	}	
	
	@Test
	public void testDefault() {
		Map<String, ServiceState> stateMap = new HashMap<String, ServiceState>();
		stateMap.put(SERVICE_NAME, ServiceState.UNKNOWN);
		Mockito.when(serviceManager.getAllStates()).thenReturn(stateMap);
		String output = SERVICE_NAME + ": " + ServiceState.UNKNOWN.toString() + StringUtils.CRLF;
		assertEquals(output, serviceCommand.execute(serviceCommand.name()));
		Mockito.verify(serviceManager).getAllStates();
	}

	@Test
	public void testUnknownService() {
		Mockito.when(serviceManager.isAvailable(Matchers.eq(SERVICE_NAME))).thenReturn(false);
		String result = serviceCommand.execute(serviceCommand.name() + Command.SEPARATOR + SERVICE_NAME + Command.SEPARATOR + ServiceManager.ACTION_STATUS);
		assertEquals(String.format(ServiceCommand.UNKNOWN_SERVICE, SERVICE_NAME), result);
	}	

	@Test
	public void testUnknownAction() {
		String actionName = "dummy";
		Mockito.when(serviceManager.isAvailable(Matchers.eq(SERVICE_NAME))).thenReturn(true);
		Mockito.when(serviceManager.isAllowedAction(Matchers.eq(SERVICE_NAME), Matchers.eq(actionName))).thenReturn(false);		
		String result = serviceCommand.execute(serviceCommand.name() + Command.SEPARATOR + SERVICE_NAME + Command.SEPARATOR + actionName);
		assertEquals(String.format(ServiceCommand.UNKNOWN_ACTION, actionName, SERVICE_NAME), result);

	}	
	
	@Test
	public void testStatus() {
		ServiceState state = ServiceState.STOPPED;
		String action = ServiceManager.ACTION_STATUS;
		Mockito.when(serviceManager.isAvailable(Matchers.eq(SERVICE_NAME))).thenReturn(true);
		Mockito.when(serviceManager.isAllowedAction(Matchers.eq(SERVICE_NAME), Matchers.eq(action))).thenReturn(true);
		Mockito.when(serviceManager.executeAction(Matchers.eq(SERVICE_NAME), Matchers.eq(action))).thenReturn(state);
		String result = serviceCommand.execute(serviceCommand.name() + Command.SEPARATOR + SERVICE_NAME + Command.SEPARATOR + action);
		assertEquals(String.format(ServiceCommand.SERVICE_STATUS, SERVICE_NAME, state.toString()), result);
	}	

	@Test
	public void testStart() {
		ServiceState state = ServiceState.RUNNING;
		String action = ServiceManager.ACTION_START;
		Mockito.when(serviceManager.isAvailable(Matchers.eq(SERVICE_NAME))).thenReturn(true);
		Mockito.when(serviceManager.isAllowedAction(Matchers.eq(SERVICE_NAME), Matchers.eq(action))).thenReturn(true);
		Mockito.when(serviceManager.executeAction(Matchers.eq(SERVICE_NAME), Matchers.eq(action))).thenReturn(state);
		String result = serviceCommand.execute(serviceCommand.name() + Command.SEPARATOR + SERVICE_NAME + Command.SEPARATOR + action);
		assertEquals(String.format(ServiceCommand.SERVICE_STATUS, SERVICE_NAME, state.toString()), result);
	}	
	
	@Test
	public void testStop() {
		ServiceState state = ServiceState.STOPPED;
		String action = ServiceManager.ACTION_STOP;
		Mockito.when(serviceManager.isAvailable(Matchers.eq(SERVICE_NAME))).thenReturn(true);
		Mockito.when(serviceManager.isAllowedAction(Matchers.eq(SERVICE_NAME), Matchers.eq(action))).thenReturn(true);
		Mockito.when(serviceManager.executeAction(Matchers.eq(SERVICE_NAME), Matchers.eq(action))).thenReturn(state);
		String result = serviceCommand.execute(serviceCommand.name() + Command.SEPARATOR + SERVICE_NAME + Command.SEPARATOR + action);
		assertEquals(String.format(ServiceCommand.SERVICE_STATUS, SERVICE_NAME, state.toString()), result);
	}	
	
	@Test
	public void testRestart() {
		ServiceState state = ServiceState.RUNNING;
		String action = ServiceManager.ACTION_RESTART;
		Mockito.when(serviceManager.isAvailable(Matchers.eq(SERVICE_NAME))).thenReturn(true);
		Mockito.when(serviceManager.isAllowedAction(Matchers.eq(SERVICE_NAME), Matchers.eq(action))).thenReturn(true);
		Mockito.when(serviceManager.executeAction(Matchers.eq(SERVICE_NAME), Matchers.eq(action))).thenReturn(state);
		String result = serviceCommand.execute(serviceCommand.name() + Command.SEPARATOR + SERVICE_NAME + Command.SEPARATOR + action);
		assertEquals(String.format(ServiceCommand.SERVICE_STATUS, SERVICE_NAME, state.toString()), result);
	}
	
}
