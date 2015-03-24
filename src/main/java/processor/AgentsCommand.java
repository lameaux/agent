package processor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import model.AgentId;
import storage.ping.PingStatus;
import storage.ping.PingStatusStorage;
import utils.StringUtils;
import agent.Agent;

public class AgentsCommand extends CommandBase implements Command {

	private static final String PARAM_ACTIVE = "active";
	private static final String PARAM_ALL = "all";

	@Override
	public String execute(String request) {

		PingStatusStorage pingStatusStorage = Agent.get().getPingStatusStorage();

		String param = PARAM_ACTIVE;

		String[] params = parameters(request);
		if (params.length == 1 && !StringUtils.nullOrEmpty(params[0])) {
			param = params[0];
		}

		if (PARAM_ACTIVE.equals(param) || PARAM_ALL.equals(param)) {

			boolean activeOnly = PARAM_ACTIVE.equals(param);

			Map<AgentId, PingStatus> pingStatuses = pingStatusStorage.getSnapshot();
			StringBuffer sb = new StringBuffer();
			for (AgentId agentId : pingStatuses.keySet()) {
				PingStatus pingStatus = pingStatuses.get(agentId);
				if (!activeOnly || pingStatus.isActive()) {
					appendAgentStatusLine(sb, agentId, pingStatus);
				}
			}
			return sb.toString();
		}
		return syntaxError();

	}

	private void appendAgentStatusLine(StringBuffer sb, AgentId agentId, PingStatus pingStatus) {

		sb.append(agentId);
		if (pingStatus.isError()) {
			sb.append(" Error: ").append(pingStatus.getMessage());
		} else {
			sb.append(" Ping: ").append(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - pingStatus.getTime())).append(" min ago");
		}

		sb.append("\r\n");
	}

	@Override
	public String help() {
		return "agents [all|active]";
	}

	@Override
	public String name() {
		return "agents";
	}

}
