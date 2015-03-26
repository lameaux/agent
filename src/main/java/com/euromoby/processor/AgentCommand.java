package com.euromoby.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.AgentManager;
import com.euromoby.model.AgentId;
import com.euromoby.utils.StringUtils;

@Component
public class AgentCommand extends CommandBase implements Command {

	private static final String PARAM_ACTIVE = "active";
	private static final String PARAM_ALL = "all";
	private static final String PARAM_ADD = "add";

	private AgentManager agentManager; 
	
	@Autowired
	public AgentCommand(AgentManager agentManager) {
		this.agentManager = agentManager;
	}
	
	@Override
	public String execute(String request) {

		String[] parameters = parameters(request);

		if (parameters.length <= 1) {
			String param = PARAM_ACTIVE;

			if (parameters.length == 1) {
				param = StringUtils.nullOrEmpty(parameters[0]) ? PARAM_ACTIVE : parameters[0];
			}

			if (PARAM_ACTIVE.equals(param) || PARAM_ALL.equals(param)) {
				StringBuffer sb = new StringBuffer();
				List<AgentId> agentList;
				if (PARAM_ACTIVE.equals(param)) {
					sb.append("Active agents:").append(StringUtils.CRLF);
					agentList = agentManager.getActive();
				} else {
					sb.append("All agents:").append(StringUtils.CRLF);
					agentList = agentManager.getAll();
				}
				if (agentList.isEmpty()) {
					sb.append("empty");
				} else {
					for (AgentId agentId : agentList) {
						sb.append(agentId.toString()).append(StringUtils.CRLF);
					}
				}
				return sb.toString();
			}
		} else if (parameters.length == 3 && PARAM_ADD.equals(parameters[0]) && !StringUtils.nullOrEmpty(parameters[1])
				&& !StringUtils.nullOrEmpty(parameters[2])) {
			AgentId agentId = new AgentId(parameters[1], Integer.parseInt(parameters[2]));
			agentManager.addAgent(agentId);
			return agentId.toString();
		}

		return syntaxError();
	}

	@Override
	public String help() {

		return "agent\t[active]\t\tshow active agents" + StringUtils.CRLF + "agent\tall\t\t\tshow all agents" + StringUtils.CRLF
				+ "agent\tadd <host> <baseport>\tadd new agent";

	}

	@Override
	public String name() {
		return "agent";
	}

}
