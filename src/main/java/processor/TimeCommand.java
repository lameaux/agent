package processor;

import java.util.Date;

public class TimeCommand implements Command {

	public String execute(String request) {
		return new Date().toString();
	}

	public boolean match(String request) {
		return "time".equalsIgnoreCase(request);
	}

}
