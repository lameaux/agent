package storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.AgentId;

public class ListValueStorage<T> {

	private Map<AgentId, List<T>> map = new HashMap<AgentId, List<T>>();

	public synchronized void add(AgentId agentId, T value) {
		List<T> list = map.get(agentId);
		if (list == null) {
			list = new ArrayList<T>();
			map.put(agentId, list);
		}
		list.add(value);
	}

	public synchronized List<T> getListValues(AgentId agentId) {
		List<T> list = map.get(agentId);
		if (list == null) {
			return new ArrayList<T>();
		}
		return new ArrayList<T>(list);
	}

}
