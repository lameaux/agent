package storage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import model.AgentId;

public class SingleValueStorage<T> {

	private Map<AgentId, T> map = new ConcurrentHashMap<AgentId, T>();

	public void set(AgentId agentId, T value) {
		map.put(agentId, value);
	}

	public T get(AgentId agentId) {
		return map.get(agentId);
	}

	public void delete(AgentId agentId) {
		map.remove(agentId);
	}
	
	public Map<AgentId, T> getSnapshot() {
		return new HashMap<AgentId, T>(map);
	}	
	
}
