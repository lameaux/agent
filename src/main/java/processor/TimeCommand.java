package processor;

import java.util.Date;

public class TimeCommand extends CommandBase implements Command {

	public String execute(String request) {
		return new Date().toString();
	}

	@Override
	public String name() {
		return "time";
	}

}
