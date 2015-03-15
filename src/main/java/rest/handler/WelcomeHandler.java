package rest.handler;

import org.restexpress.Request;
import org.restexpress.Response;

import processor.CommandProcessor;
import processor.VersionCommand;

public class WelcomeHandler extends HandlerBase {

	public WelcomeHandler(CommandProcessor commandProcessor) {
		super(commandProcessor);
	}

	public Object read(Request request, Response response) {

		String version = commandProcessor.process(VersionCommand.NAME);

		response.setContentType("text/html");
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html><html><head><title>");
		sb.append(version);
		sb.append("</title></head><body>");
		sb.append(version);
		sb.append("</body></html>");
		return sb.toString();
	}
}
