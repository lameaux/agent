package processor;

public class WhoCommand extends CommandBase implements Command {

	public String execute(String request) {
		return "anonymous";
	}

	@Override
	public String name() {
		return "who";
	}

}
