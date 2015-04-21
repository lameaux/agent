package com.euromoby.cdn;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.AgentManager;
import com.euromoby.agent.Config;
import com.euromoby.http.HttpClientProvider;

@RunWith(MockitoJUnitRunner.class)
public class CdnNetworkTest {

	@Mock
	Config config;
	@Mock
	AgentManager agentManager;
	@Mock
	HttpClientProvider httpClientProvider;

	CdnNetwork cdnNetwork;

	@Before
	public void init() {
		Mockito.when(config.getCdnPoolSize()).thenReturn(1);
		cdnNetwork = new CdnNetwork(config, agentManager, httpClientProvider);
	}
	
}
