package processor;

import org.restexpress.Request;

public interface Command {

	String execute(String request);

	String execute(Request request);

	boolean match(String request);

	String name();

	String help();
}
