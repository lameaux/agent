package processor;

import java.util.List;

import model.AgentId;
import utils.StringUtils;
import agent.Agent;
import agent.AgentManager;

public class AgentCommand extends CommandBase implements Command {

	private static final String PARAM_ACTIVE = "active";
	private static final String PARAM_ALL = "all";
	private static final String PARAM_ADD = "add";	

	@Override
	public String execute(String request) {

		AgentManager agentManager = Agent.get().getAgentManager();
		
		String[] parameters = parameters(request);
		
		if (parameters.length <= 1) {
			String param = PARAM_ACTIVE;
			
			if (parameters.length == 1) {
				param = StringUtils.nullOrEmpty(parameters[0]) ? PARAM_ACTIVE : parameters[0];
			}
			
			if (PARAM_ACTIVE.equals(param) || PARAM_ALL.equals(param)) {
				List<AgentId> agentList;
				if (PARAM_ACTIVE.equals(param)) {
					agentList = agentManager.getActive();
				} else {
					agentList = agentManager.getAll();
				}

				StringBuffer sb = new StringBuffer();
				for (AgentId agentId : agentList) {
					sb.append(agentId.toString()).append(StringUtils.CRLF);
				}
				return sb.toString();
			}		
		} else if (parameters.length == 3 && PARAM_ADD.equals(parameters[0]) && !StringUtils.nullOrEmpty(parameters[1]) && !StringUtils.nullOrEmpty(parameters[2])) {
				AgentId agentId = new AgentId(parameters[1], Integer.parseInt(parameters[2]));
				agentManager.addAgent(agentId);
				return agentId.toString();
		}

		return syntaxError();
	}

	@Override
	public String help() {
		return "agent [active|all|add host baseport]";
	}

	@Override
	public String name() {
		return "agent";
	}

}
