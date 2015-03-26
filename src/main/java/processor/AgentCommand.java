package processor;

import java.util.List;

import model.AgentId;
import utils.StringUtils;
import agent.Agent;
import agent.AgentManager;

public class AgentCommand extends CommandBase implements Command {

	private static final String PARAM_ACTIVE = "active";
	private static final String PARAM_ALL = "all";

	@Override
	public String execute(String request) {

		AgentManager agentManager = Agent.get().getAgentManager();
		
		String[] params = parameters(request);
		
		if (params.length == 1) {
			String param = StringUtils.nullOrEmpty(params[0]) ? PARAM_ACTIVE : params[0];
			
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
		}

		return syntaxError();
	}

	@Override
	public String help() {
		return "agent [active|all|add host:port]";
	}

	@Override
	public String name() {
		return "agent";
	}

}
